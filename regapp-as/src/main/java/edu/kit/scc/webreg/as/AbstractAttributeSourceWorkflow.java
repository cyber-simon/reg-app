package edu.kit.scc.webreg.as;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueEntity_;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueStringEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceGroupEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceGroupEntity_;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.tools.PropertyReader;

public abstract class AbstractAttributeSourceWorkflow implements AttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LoggerFactory.getLogger(AbstractAttributeSourceWorkflow.class);

	protected PropertyReader prop;

	protected String groupKey;
	protected String groupSeparator;

	protected String projectKey;
	protected String projectSeparator;

	private ASUserAttrEntity asUserAttr;
	private ASUserAttrValueDao asValueDao;
	private GroupDao groupDao;
	private AttributeSourceGroupDao attributeSourceGroupDao;
	private AttributeSourceAuditor auditor;

	public void init(ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, GroupDao groupDao,
			AttributeSourceAuditor auditor) throws UserUpdateException {
		this.asUserAttr = asUserAttr;
		this.asValueDao = asValueDao;
		this.groupDao = groupDao;
		this.auditor = auditor;

		attributeSourceGroupDao = groupDao.getAttributeSourceGroupDao();

		try {
			prop = new PropertyReader(asUserAttr.getAttributeSource().getAsProps());

			groupKey = prop.readPropOrNull("group_key");
			if (prop.readPropOrNull("group_separator") != null)
				groupSeparator = prop.readPropOrNull("group_separator");
			else
				groupSeparator = ";";

			projectKey = prop.readPropOrNull("project_key");
			if (prop.readPropOrNull("project_separator") != null)
				projectSeparator = prop.readPropOrNull("project_separator");
			else
				projectSeparator = ";";

		} catch (PropertyReaderException e) {
			throw new UserUpdateException(e);
		}
	}

	protected Boolean createOrUpdateValues(Map<String, Object> valueMap) {
		Boolean changed = false;

		List<ASUserAttrValueEntity> valueList = asValueDao
				.findAll(equal(ASUserAttrValueEntity_.asUserAttr, asUserAttr));

		logger.debug("{} values already present for {}", valueList.size(), asUserAttr.getAttributeSource().getName());

		Map<String, ASUserAttrValueEntity> keysToUpdate = new HashMap<String, ASUserAttrValueEntity>();
		Map<String, ASUserAttrValueEntity> keysToDelete = new HashMap<String, ASUserAttrValueEntity>();

		for (ASUserAttrValueEntity value : valueList) {
			if (valueMap.containsKey(value.getKey())) {
				keysToUpdate.put(value.getKey(), value);
			} else {
				keysToDelete.put(value.getKey(), value);
			}
		}

		Set<String> keysToAdd = new HashSet<String>(valueMap.keySet());
		keysToAdd.removeAll(keysToUpdate.keySet());

		logger.debug("Marking {} for update, {} for add and {} for delete", keysToUpdate.size(), keysToAdd.size(),
				keysToDelete.size());

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
			ASUserAttrValueStringEntity asValue = new ASUserAttrValueStringEntity();
			asValue.setKey(key);
			asValue.setAsUserAttr(asUserAttr);
			asValue.setValueString((String) o);
			auditor.logAction("as-workflow", "CREATE VALUE (String)", key, "", AuditStatus.SUCCESS);
			asValue = (ASUserAttrValueStringEntity) asValueDao.persist(asValue);

			if (groupKey != null && key.equals(groupKey)) {
				processGroups(asValue);
			}

			if (projectKey != null && key.equals(projectKey)) {
				processGroups(asValue);
			}
		} else {
			logger.warn("Cannot process value of type {}", o.getClass());
			return false;
		}

		return true;
	}

	private Boolean deleteValue(String key, ASUserAttrValueEntity asValue) {

		logger.debug("Deleting value for key {}", key);
		auditor.logAction("as-workflow", "DELETE VALUE", key, "", AuditStatus.SUCCESS);
		asValueDao.delete(asValue);

		if (groupKey != null && key.equals(groupKey)) {
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

			if (!s.equals(asStringValue.getValueString())) {
				auditor.logAction("as-workflow", "UPDATE VALUE (String)", key,
						asStringValue.getValueString() + " -> " + s, AuditStatus.SUCCESS);
				logger.debug("Updating value for key {}: {} -> {}", key, asStringValue.getValueString(), s);
				asStringValue.setValueString(s);
				changed = true;
			}

			if (groupKey != null && key.equals(groupKey)) {
				changed |= processGroups(asStringValue);
			}
		} else {
			logger.warn("Value change for key {} from {} to ASUserAttrValueStringEntity not supported yet", key,
					o.getClass());
		}

		return changed;
	}

	private Boolean processProjects(ASUserAttrValueStringEntity asValue) {
		Boolean changed = false;
		
		UserEntity user = asUserAttr.getUser();
		AttributeSourceEntity attributeSource = asUserAttr.getAttributeSource();

		//CDI.current().select(AttributeSourceProjectUpdater.class).get();
		
		if (asValue == null || asValue.getValueString() == null || asValue.getValueString().equals("")) {
			// delete all projects for this user
			
			changed = true;
		} else {
			
		}
		
		return changed;
	}	
	
	private Boolean processGroups(ASUserAttrValueStringEntity asValue) {

		Boolean changed = false;
		HashSet<GroupEntity> allChangedGroups = new HashSet<GroupEntity>();

		UserEntity user = asUserAttr.getUser();
		AttributeSourceEntity attributeSource = asUserAttr.getAttributeSource();
		List<AttributeSourceGroupEntity> oldGroupList = attributeSourceGroupDao.findByUserAndAS(asUserAttr.getUser(),
				attributeSource);

		if (asValue == null || asValue.getValueString() == null || asValue.getValueString().equals("")) {
			// delete all groups for this user
			for (AttributeSourceGroupEntity group : oldGroupList) {
				logger.debug("Removeing {} grom group {}", user.getEppn(), group.getName());
				groupDao.removeUserGromGroup(user, group);
				allChangedGroups.add(group);
			}
			changed = true;
		} else {
			String[] groupsString = asValue.getValueString().toLowerCase().split(groupSeparator);
			Set<String> newGroups = new HashSet<String>(Arrays.asList(groupsString));

			Map<String, AttributeSourceGroupEntity> oldGroupsMap = new HashMap<String, AttributeSourceGroupEntity>();
			for (AttributeSourceGroupEntity group : oldGroupList) {
				oldGroupsMap.put(group.getName(), group);
			}

			Set<String> groupsToRemove = new HashSet<String>(oldGroupsMap.keySet());
			groupsToRemove.removeAll(newGroups);

			for (String s : groupsToRemove) {
				logger.debug("Removing {} grom group {}", user.getEppn(), s);
				groupDao.removeUserGromGroup(user, oldGroupsMap.get(s));
				allChangedGroups.add(oldGroupsMap.get(s));
			}

			Set<String> groupsToAdd = new HashSet<String>(newGroups);
			groupsToAdd.removeAll(oldGroupsMap.keySet());

			for (String s : groupsToAdd) {
				AttributeSourceGroupEntity group = findASGroupByNameAndAS(s, attributeSource);
				if (group == null) {
					logger.debug("Creating group {}", s);
					group = attributeSourceGroupDao.createNew();
					group.setName(s);
					group.setAttributeSource(attributeSource);
					group.setGidNumber(groupDao.getNextGID().intValue());
					Set<ServiceEntity> services = new HashSet<ServiceEntity>();
					for (AttributeSourceServiceEntity asse : attributeSource.getAttributeSourceServices())
						services.add(asse.getService());
					group = (AttributeSourceGroupEntity) groupDao.persistWithServiceFlags(group, services);
				}

				logger.debug("Adding {} to group {}", user.getEppn(), s);
				groupDao.addUserToGroup(user, group);
				allChangedGroups.add(group);
			}
		}

		for (GroupEntity group : allChangedGroups) {
			if (group instanceof ServiceBasedGroupEntity) {
				groupDao.setServiceFlags((ServiceBasedGroupEntity) group, ServiceGroupStatus.DIRTY);
			}
		}

		if (allChangedGroups.size() > 0) {
			EventSubmitter eventSubmitter;
			try {
				InitialContext ic = new InitialContext();
				eventSubmitter = (EventSubmitter) ic.lookup(
						"global/bwreg/bwreg-service/EventSubmitterImpl!edu.kit.scc.webreg.event.EventSubmitter");
				MultipleGroupEvent mge = new MultipleGroupEvent(allChangedGroups);
				eventSubmitter.submit(mge, EventType.GROUP_UPDATE, auditor.getActualExecutor());
			} catch (NamingException e) {
				logger.warn("Exeption", e);
			} catch (EventSubmitException e) {
				logger.warn("Exeption", e);
			}
		}
		return changed;
	}

	private AttributeSourceGroupEntity findASGroupByNameAndAS(String name, AttributeSourceEntity attributeSource) {
		return attributeSourceGroupDao.find(and(equal(AttributeSourceGroupEntity_.name, name),
				equal(AttributeSourceGroupEntity_.attributeSource, attributeSource)));
	}

}
