/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.impl;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.GroupServiceHook;
import edu.kit.scc.webreg.service.HomeOrgGroupService;
import edu.kit.scc.webreg.service.SerialService;

@Stateless
public class HomeOrgGroupServiceImpl extends BaseServiceImpl<HomeOrgGroupEntity, Long> implements HomeOrgGroupService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private HookManager hookManager;
	
	@Inject
	private HomeOrgGroupDao dao;
	
	@Inject
	private GroupDao groupDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;
	
	@Inject
	private AttributeMapHelper attrHelper;

	@Inject 
	private SerialService serialService;

	@Inject 
	private EventSubmitter eventSubmitter;
	
	@Override
	public Set<GroupEntity> updateGroupsForUser(UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws RegisterException {
		HashSet<GroupEntity> changedGroups = new HashSet<GroupEntity>();
		
		changedGroups.addAll(updatePrimary(user, attributeMap, auditor));
		changedGroups.addAll(updateSecondary(user, attributeMap, auditor));

		// Also add parent groups, to reflect changes
		HashSet<GroupEntity> allChangedGroups = new HashSet<GroupEntity>(changedGroups.size());
		for (GroupEntity group : changedGroups) {
			allChangedGroups.add(group);
			if (group.getParents() != null) {
				for (GroupEntity parent : group.getParents()) {
					logger.debug("Adding parent group to changed groups: {}", parent.getName());
					allChangedGroups.add(parent);
				}
			}
		}
		
		for (GroupEntity group : allChangedGroups) {
			if (group instanceof ServiceBasedGroupEntity) {
				List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByGroup((ServiceBasedGroupEntity) group);
				for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
					groupFlag.setStatus(ServiceGroupStatus.DIRTY);
					groupFlagDao.persist(groupFlag);
				}
			}
		}
		
		MultipleGroupEvent mge = new MultipleGroupEvent(allChangedGroups);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, auditor.getActualExecutor());
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
		
		return allChangedGroups;
	}
	
	protected Set<GroupEntity> updatePrimary(UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws RegisterException {
		Set<GroupEntity> changedGroups = new HashSet<GroupEntity>();

		GroupServiceHook completeOverrideHook = null;
		Set<GroupServiceHook> activeHooks = new HashSet<GroupServiceHook>();

		HomeOrgGroupEntity group = null;

		for (GroupServiceHook hook : hookManager.getGroupHooks()) {
			if (hook.isPrimaryResponsible(user, attributeMap)) {
				group = hook.preUpdateUserPrimaryGroupFromAttribute(dao, groupDao, group, user, attributeMap, auditor, changedGroups);
				activeHooks.add(hook);
				
				if (hook.isPrimaryCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}
		
		if (completeOverrideHook == null) {
			
			String homeId = attrHelper.getSingleStringFirst(attributeMap, "http://bwidm.de/bwidmOrgId");
	
			if (homeId == null) {
				logger.warn("No Home ID is set for User {}, resetting primary group", user.getEppn());
			}
			else {
				//Filter all non character from homeid
				homeId = homeId.toLowerCase();
				homeId = homeId.replaceAll("[^a-z]", "");
				
				String groupName = attrHelper.getSingleStringFirst(attributeMap, "http://bwidm.de/bwidmCC");
				if (groupName == null) {
					groupName = homeId;
				}
				else {
					//Filter all non character from groupName
					groupName = Normalizer.normalize(groupName, Normalizer.Form.NFD);
					groupName = groupName.toLowerCase();
					groupName = groupName.replaceAll("[^a-z0-9\\-_]", "");
				}
				
				logger.info("Setting standard HomeID group for user {}", user.getEppn());
				group = dao.findByNameAndPrefix(groupName, homeId);
				
				if (group == null) {
					group = dao.createNew();
					group.setUsers(new HashSet<UserGroupEntity>());
					group.setName(groupName);
					auditor.logAction(group.getName(), "SET FIELD", "name", group.getName(), AuditStatus.SUCCESS);
					group.setPrefix(homeId);
					auditor.logAction(group.getName(), "SET FIELD", "prefix", group.getPrefix(), AuditStatus.SUCCESS);
					group.setGidNumber(serialService.next("gid-number-serial").intValue());
					auditor.logAction(group.getName(), "SET FIELD", "gidNumber", "" + group.getGidNumber(), AuditStatus.SUCCESS);
					group.setIdp(user.getIdp());
					auditor.logAction(group.getName(), "SET FIELD", "idpEntityId", "" + user.getIdp().getEntityId(), AuditStatus.SUCCESS);
					group = dao.persistWithServiceFlags(group);
					auditor.logAction(group.getName(), "CREATE GROUP", null, "Group created", AuditStatus.SUCCESS);
					
					changedGroups.add(group);
				}
			}
		}
		else {
			logger.info("Overriding standard Primary Group Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
		}
		
		if (group == null) {
			logger.warn("No Primary Group for user {}", user.getEppn());
		}

		for (GroupServiceHook hook : activeHooks) {
			group = hook.postUpdateUserPrimaryGroupFromAttribute(dao, groupDao, group, user, attributeMap, auditor, changedGroups);
		}
		
		if (user.getPrimaryGroup() != null && (! user.getPrimaryGroup().equals(group))) {
			if (group == null) {
				auditor.logAction(user.getEppn(), "UPDATE FIELD", "primaryGroup", 
						user.getPrimaryGroup().getName() + " (" + user.getPrimaryGroup().getGidNumber() + ") -> " + 
						"null", AuditStatus.SUCCESS);
			}
			else {
				auditor.logAction(user.getEppn(), "UPDATE FIELD", "primaryGroup", 
					user.getPrimaryGroup().getName() + " (" + user.getPrimaryGroup().getGidNumber() + ") -> " + 
					group.getName() + " (" + group.getGidNumber() + ")", AuditStatus.SUCCESS);
			}
		}
		else if (user.getPrimaryGroup() == null && group != null) {
			auditor.logAction(user.getEppn(), "UPDATE FIELD", "primaryGroup", 
					"null -> " + 
					group.getName() + " (" + group.getGidNumber() + ")", AuditStatus.SUCCESS);
		}

		user.setPrimaryGroup(group);
		
		return changedGroups;
	}	

	protected Set<GroupEntity> updateSecondary(UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws RegisterException {
		Set<GroupEntity> changedGroups = new HashSet<GroupEntity>();

		GroupServiceHook completeOverrideHook = null;
		Set<GroupServiceHook> activeHooks = new HashSet<GroupServiceHook>();

		for (GroupServiceHook hook : hookManager.getGroupHooks()) {
			if (hook.isSecondaryResponsible(user, attributeMap)) {
				hook.preUpdateUserSecondaryGroupFromAttribute(dao, groupDao, user, attributeMap, auditor, changedGroups);
				activeHooks.add(hook);
				
				if (hook.isSecondaryCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}
				

		if (completeOverrideHook == null) {

			String homeId = attrHelper.getSingleStringFirst(attributeMap, "http://bwidm.de/bwidmOrgId");


			List<String> groupList = new ArrayList<String>();

			if (homeId == null) {
				logger.warn("No Home ID is set for User {}, resetting secondary groups", user.getEppn());
			}
			else if (attributeMap.get("http://bwidm.de/bwidmMemberOf") == null) {
				logger.info("No http://bwidm.de/bwidmMemberOf is set. Resetting secondary groups");
			}
			else {
				List<String> groupsFromAttr = attrHelper.attributeListToStringList(attributeMap, "http://bwidm.de/bwidmMemberOf");
				
				//Check if a group name contains a ';', and divide this group
				for (String group : groupsFromAttr) {
					if (group.contains(";")) {
						String[] splitGroups = group.split(";");
						for (String g : splitGroups) {
							groupList.add(filterGroup(g));
						}
					}
					else {
						groupList.add(filterGroup(group));
					}
				}
			}
			
			if (user.getGroups() == null)
				user.setGroups(new HashSet<UserGroupEntity>());

			Set<GroupEntity> groupsFromAssertion = new HashSet<GroupEntity>();

			logger.debug("Looking up groups from database");
			Map<String, HomeOrgGroupEntity> dbGroupMap = new HashMap<String, HomeOrgGroupEntity>();
			logger.debug("Indexing groups from database");
			for (HomeOrgGroupEntity dbGroup : dao.findByNameListAndPrefix(groupList, homeId)) {
				dbGroupMap.put(dbGroup.getName(), dbGroup);
			}
			
			for (String group : groupList) {
				if (group != null && (!group.equals(""))) {

					logger.debug("Analyzing group {}", group);
					HomeOrgGroupEntity groupEntity = dbGroupMap.get(group);
					
					try {
						if (groupEntity == null) {
							int gidNumber = serialService.next("gid-number-serial").intValue();
							logger.info("Creating group {} with gidNumber {}", group, gidNumber);
							groupEntity = dao.createNew();

							groupEntity.setUsers(new HashSet<UserGroupEntity>());
							groupEntity.setParents(new HashSet<GroupEntity>());
							groupEntity.setName(group);
							auditor.logAction(groupEntity.getName(), "SET FIELD", "name", groupEntity.getName(), AuditStatus.SUCCESS);
							groupEntity.setPrefix(homeId);
							auditor.logAction(groupEntity.getName(), "SET FIELD", "prefix", groupEntity.getPrefix(), AuditStatus.SUCCESS);
							groupEntity.setGidNumber(gidNumber);
							auditor.logAction(groupEntity.getName(), "SET FIELD", "gidNumber", "" + groupEntity.getGidNumber(), AuditStatus.SUCCESS);
							groupEntity.setIdp(user.getIdp());
							auditor.logAction(groupEntity.getName(), "SET FIELD", "idpEntityId", "" + user.getIdp().getEntityId(), AuditStatus.SUCCESS);
							groupEntity = dao.persistWithServiceFlags(groupEntity);
							auditor.logAction(groupEntity.getName(), "CREATE GROUP", null, "Group created", AuditStatus.SUCCESS);
							
							changedGroups.add(groupEntity);
						}
						
						if (groupEntity != null) {
							groupsFromAssertion.add(groupEntity);

							if (! groupDao.isUserInGroup(user, groupEntity)) {
								logger.debug("Adding user {} to group {}", user.getEppn(), groupEntity.getName());
								groupDao.addUserToGroup(user, groupEntity);
								changedGroups.remove(groupEntity);
								groupEntity = dao.persist(groupEntity);
								auditor.logAction(user.getEppn(), "ADD TO GROUP", groupEntity.getName(), null, AuditStatus.SUCCESS);

								changedGroups.add(groupEntity);
							}
						}
						
					} catch (NumberFormatException e) {
						logger.warn("GidNumber has a bad number format: {}", e.getMessage());
					}
				}
			}
			
			Set<GroupEntity> groupsToRemove = new HashSet<GroupEntity>(groupDao.findByUser(user));
			groupsToRemove.removeAll(groupsFromAssertion);

			for (GroupEntity removeGroup : groupsToRemove) {
				if (removeGroup instanceof HomeOrgGroupEntity) {
					if (! removeGroup.equals(user.getPrimaryGroup())) {
						logger.debug("Removing user {} from group {}", user.getEppn(), removeGroup.getName());
						groupDao.removeUserGromGroup(user, removeGroup);
						
						auditor.logAction(user.getEppn(), "REMOVE FROM GROUP", removeGroup.getName(), null, AuditStatus.SUCCESS);
	
						changedGroups.add(removeGroup);
					}
				}
				else {
					logger.debug("Group {} of type {}. Doing nothing.", removeGroup.getName(), removeGroup.getClass().getSimpleName());
				}
			}

			/*
			 * Add Primary group to secondary as well
			 */
			if (user.getPrimaryGroup() != null && (! groupDao.isUserInGroup(user, user.getPrimaryGroup()))) {
				logger.debug("Adding user {} to his primary group {} as secondary", user.getEppn(), user.getPrimaryGroup().getName());
				groupDao.addUserToGroup(user, user.getPrimaryGroup());
				changedGroups.add(user.getPrimaryGroup());
			}
		}
		else {
			logger.info("Overriding standard Secondary Group Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
		}
			
		for (GroupServiceHook hook : hookManager.getGroupHooks()) {
			hook.postUpdateUserSecondaryGroupFromAttribute(dao, groupDao, user, attributeMap, auditor, changedGroups);
		}
		
		return changedGroups;
	}
	
	private String filterGroup(String groupName) {
		//Filter all non character from groupName
		groupName = Normalizer.normalize(groupName, Normalizer.Form.NFD);
		groupName = groupName.toLowerCase();
		groupName = groupName.replaceAll("[^a-z0-9\\-_]", "");
		
		return groupName;
	}
	
	@Override
	public HomeOrgGroupEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}	
	
	@Override
	public HomeOrgGroupEntity findByName(String name) {
		return dao.findByName(name);
	}	
	
	@Override
	public List<HomeOrgGroupEntity> findByUser(UserEntity user) {
		return dao.findByUser(user);
	}	
	
	@Override
	public HomeOrgGroupEntity persistWithServiceFlags(HomeOrgGroupEntity entity) {
		return dao.persistWithServiceFlags(entity);
	}	
	
	@Override
	protected BaseDao<HomeOrgGroupEntity, Long> getDao() {
		return dao;
	}	
}
