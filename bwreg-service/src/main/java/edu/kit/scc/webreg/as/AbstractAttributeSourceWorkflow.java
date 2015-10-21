package edu.kit.scc.webreg.as;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueStringEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceGroupEntity;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public abstract class AbstractAttributeSourceWorkflow implements AttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LoggerFactory.getLogger(AbstractAttributeSourceWorkflow.class);

	protected PropertyReader prop;

	protected String groupKey;
	protected String groupSeparator;
	
	private ASUserAttrEntity asUserAttr;
	private ASUserAttrValueDao asValueDao;
	private GroupDao groupDao;
	private AttributeSourceGroupDao attributeSourceGroupDao;
	private AttributeSourceAuditor auditor;
	
	public void init(ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, GroupDao groupDao,
			AttributeSourceAuditor auditor)
			throws UserUpdateException {
		this.asUserAttr = asUserAttr;
		this.asValueDao = asValueDao;
		this.groupDao = groupDao;
		this.auditor = auditor;
		
		attributeSourceGroupDao = groupDao.getAttributeSourceGroupDao();
		
		try {
			prop = new PropertyReader(asUserAttr.getAttributeSource().getAsProps());
			
			if (prop.readPropOrNull("group_key") != null)
				groupKey = prop.readPropOrNull("group_key");
			else
				groupKey = "group";

			if (prop.readPropOrNull("group_separator") != null)
				groupSeparator = prop.readPropOrNull("group_separator");
			else
				groupSeparator = ";";
			
		} catch (PropertyReaderException e) {
			throw new UserUpdateException(e);
		}
	}
	
	protected Boolean createOrUpdateValues(Map<String, Object> valueMap) {
		Boolean changed = false;
		
		List<ASUserAttrValueEntity> valueList = asValueDao.findValues(asUserAttr);
		
		logger.debug("{} values already present for {}", valueList.size(), asUserAttr.getAttributeSource().getName());
		
		Map<String, ASUserAttrValueEntity> keysToUpdate = new HashMap<String, ASUserAttrValueEntity>();
		Map<String, ASUserAttrValueEntity> keysToDelete = new HashMap<String, ASUserAttrValueEntity>();
		
		for (ASUserAttrValueEntity value : valueList) {
			if (valueMap.containsKey(value.getKey())) {
				keysToUpdate.put(value.getKey(), value);
			}
			else {
				keysToDelete.put(value.getKey(), value);
			}
		}

		Set<String> keysToAdd = new HashSet<String>(valueMap.keySet());
		keysToAdd.removeAll(keysToUpdate.keySet());
		
		logger.debug("Marking {} for update, {} for add and {} for delete", keysToUpdate.size(), keysToAdd.size(), keysToDelete.size());

		for (String key : keysToAdd) {
			changed |= createValue(key, valueMap.get(key));
		}
		
		for (Entry<String, ASUserAttrValueEntity> entry : keysToDelete.entrySet()) {
			changed |= deleteValue(entry.getKey(), entry.getValue());
		}
		
		for (Entry<String, ASUserAttrValueEntity> entry : keysToUpdate.entrySet()) {
			changed |= updateValue(entry.getKey(), entry.getValue(), valueMap.get(entry.getKey()));
		}
		
		return changed;
	}

	private Boolean createValue(String key, Object o) {
		if (o == null) {
			logger.warn("Cannot process null value");
			return false;
		}

		logger.debug("Creating value for key {} of type {}", key, o.getClass().getName());
		
		if (o instanceof String) {
			ASUserAttrValueStringEntity asValue = asValueDao.createNewString();
			asValue.setKey(key);
			asValue.setAsUserAttr(asUserAttr);
			asValue.setValueString((String) o);
			auditor.logAction("as-workflow", "CREATE VALUE (String)", key, "", AuditStatus.SUCCESS);
			asValue = (ASUserAttrValueStringEntity) asValueDao.persist(asValue);
			
			if (key.equals(groupKey)) {
				processGroups(asValue);
			}
		}
		else { 
			logger.warn("Cannot process value of type {}", o.getClass());
			return false;
		}
		
		return true;
	}
	
	private Boolean deleteValue(String key, ASUserAttrValueEntity asValue) {
		
		logger.debug("Deleting value for key {}", key);
		auditor.logAction("as-workflow", "DELETE VALUE", key, "", AuditStatus.SUCCESS);
		asValueDao.delete(asValue);

		if (key.equals(groupKey)) {
			processGroups(null);
		}

		return true;
	}
	
	private Boolean updateValue(String key, ASUserAttrValueEntity asValue, Object o) {
		
		Boolean changed = false;
		logger.debug("Updating value for key {}", key);
		
		if (o instanceof String && asValue instanceof ASUserAttrValueStringEntity) {
			String s = (String) o;
			ASUserAttrValueStringEntity asStringValue = (ASUserAttrValueStringEntity) asValue;
			
			if (! s.equals(asStringValue.getValueString())) {
				asStringValue.setValueString(s);
				auditor.logAction("as-workflow", "UPDATE VALUE (String)", key, asStringValue.getValueString() + " -> " + s, AuditStatus.SUCCESS);
				logger.debug("Updating value for key {}: {} -> {}", key, asStringValue.getValueString(), s);
				changed = true;
			}
			
			if (key.equals(groupKey)) {
				changed |= processGroups(asStringValue);
			}
		}
		else {
			logger.warn("Value change for key {} from {} to ASUserAttrValueStringEntity not supported yet", key, o.getClass());
		}
		
		return changed;
	}
	
	private Boolean processGroups(ASUserAttrValueStringEntity asValue) {
	
		Boolean changed = false;

		UserEntity user = asUserAttr.getUser();
		AttributeSourceEntity attributeSource = asUserAttr.getAttributeSource();
		List<AttributeSourceGroupEntity> oldGroupList = attributeSourceGroupDao.findByUserAndAS(asUserAttr.getUser(), attributeSource);
		
		if (asValue == null || asValue.getValueString() == null || asValue.getValueString().equals("")) {
			//delete all groups for this user
			for (AttributeSourceGroupEntity group : oldGroupList) {
				groupDao.removeUserGromGroup(user, group);
			}
			changed = true;
		}
		else {
			String[] groupsString = asValue.getValueString().split(groupSeparator);
			Set<String> newGroups = new HashSet<String>(Arrays.asList(groupsString));
			
			Map<String, AttributeSourceGroupEntity> oldGroupsMap = new HashMap<String, AttributeSourceGroupEntity>();
			for (AttributeSourceGroupEntity group : oldGroupList) {
				oldGroupsMap.put(group.getName(), group);
			}

			Set<String> groupsToRemove = new HashSet<String>(oldGroupsMap.keySet());
			groupsToRemove.removeAll(newGroups);
			
			for(String s : groupsToRemove) {
				logger.debug("Removeing {} grom group {}", user.getEppn(), s);
				groupDao.removeUserGromGroup(user, oldGroupsMap.get(s));
			}
			
			Set<String> groupsToAdd = new HashSet<String>(newGroups);
			groupsToAdd.removeAll(oldGroupsMap.keySet());
			
			for (String s : groupsToAdd) {
				AttributeSourceGroupEntity group = attributeSourceGroupDao.findByNameAndAS(s, attributeSource);
				if (group == null) {
					logger.debug("Creating group {}", s);
					group = attributeSourceGroupDao.createNew();
					group.setName(s);
					group.setAttributeSource(attributeSource);
					
				}
			}
		}
		
		return changed;
	}	
}
