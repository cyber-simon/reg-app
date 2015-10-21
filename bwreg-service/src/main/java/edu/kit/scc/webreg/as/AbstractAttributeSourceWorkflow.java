package edu.kit.scc.webreg.as;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueStringEntity;
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
	
	public void init(ASUserAttrEntity asUserAttr)
			throws UserUpdateException {
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
	
	protected Boolean createOrUpdateValues(Map<String, Object> valueMap, ASUserAttrEntity asUserAttr, 
			ASUserAttrValueDao asValueDao, AttributeSourceGroupDao attributeSourceGroupDao, AttributeSourceAuditor auditor) {
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
			changed |= createValue(key, valueMap.get(key), asUserAttr, asValueDao, attributeSourceGroupDao, auditor);
		}
		
		for (Entry<String, ASUserAttrValueEntity> entry : keysToDelete.entrySet()) {
			changed |= deleteValue(entry.getKey(), entry.getValue(), asUserAttr, asValueDao, attributeSourceGroupDao, auditor);
		}
		
		for (Entry<String, ASUserAttrValueEntity> entry : keysToUpdate.entrySet()) {
			changed |= updateValue(entry.getKey(), entry.getValue(), valueMap.get(entry.getKey()), asUserAttr, asValueDao, attributeSourceGroupDao, auditor);
		}
		
		return changed;
	}

	private Boolean createValue(String key, Object o, ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, 
			AttributeSourceGroupDao attributeSourceGroupDao, AttributeSourceAuditor auditor) {
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
				processGroups(asValue, asUserAttr, asValueDao, attributeSourceGroupDao, auditor);
			}
		}
		else { 
			logger.warn("Cannot process value of type {}", o.getClass());
			return false;
		}
		
		return true;
	}
	
	private Boolean deleteValue(String key, ASUserAttrValueEntity asValue, ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, 
			AttributeSourceGroupDao attributeSourceGroupDao, AttributeSourceAuditor auditor) {
		
		logger.debug("Deleting value for key {}", key);
		auditor.logAction("as-workflow", "DELETE VALUE", key, "", AuditStatus.SUCCESS);
		asValueDao.delete(asValue);

		if (key.equals(groupKey)) {
			processGroups(null, asUserAttr, asValueDao, attributeSourceGroupDao, auditor);
		}

		return true;
	}
	
	private Boolean updateValue(String key, ASUserAttrValueEntity asValue, Object o, ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, 
			AttributeSourceGroupDao attributeSourceGroupDao, AttributeSourceAuditor auditor) {
		
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
				changed |= processGroups(asStringValue, asUserAttr, asValueDao, attributeSourceGroupDao, auditor);
			}
		}
		else {
			logger.warn("Value change for key {} from {} to ASUserAttrValueStringEntity not supported yet", key, o.getClass());
		}
		
		return changed;
	}
	
	private Boolean processGroups(ASUserAttrValueStringEntity asValue, ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, 
			AttributeSourceGroupDao attributeSourceGroupDao, AttributeSourceAuditor auditor) {
	
		Boolean changed = false;
		
		if (asValue == null || asValue.getValueString() == null || asValue.getValueString().equals("")) {
			//delete all groups for this user
			changed = true;
		}
		else {
			String[] groupsString = asValue.getValueString().split(groupSeparator);
			List<AttributeSourceGroupEntity> oldGroupList = attributeSourceGroupDao.findByUserAndAS(asUserAttr.getUser(), asUserAttr.getAttributeSource());
			
			
		}
		
		return changed;
	}	
}
