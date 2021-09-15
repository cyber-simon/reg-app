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
package edu.kit.scc.webreg.hook;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;

public class UserStoreLocalGroupHook implements GroupServiceHook {

	private Logger logger = LoggerFactory.getLogger(HttpCallbackHook.class);
	
	private ApplicationConfig appConfig;
	
	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public GroupEntity preUpdateUserPrimaryGroupFromAttribute(
			HomeOrgGroupDao dao, GroupDao groupDao, GroupEntity group,
			UserEntity user, Map<String, List<Object>> attributeMap,
			Auditor auditor, Set<GroupEntity> changedGroups)
			throws UserUpdateException {
		return group;
	}

	@Override
	public GroupEntity postUpdateUserPrimaryGroupFromAttribute(
			HomeOrgGroupDao dao, GroupDao groupDao, GroupEntity group,
			UserEntity user, Map<String, List<Object>> attributeMap,
			Auditor auditor, Set<GroupEntity> changedGroups)
			throws UserUpdateException {
		return group;
	}

	@Override
	public void preUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao,
			GroupDao groupDao, UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor,
			Set<GroupEntity> changedGroups) throws UserUpdateException {
	}

	@Override
	public void postUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao,
			GroupDao groupDao, UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor,
			Set<GroupEntity> changedGroups) throws UserUpdateException {

		long start = System.currentTimeMillis();
		
		LocalGroupDao localGroupDao = groupDao.getLocalGroupDao();
		
		logger.debug("UserStoreLocalGroupHook postUpdateSecondaryGroupFromAttribute for user {}", user.getEppn());
		
		String groupLine = appConfig.getConfigValue("UserStoreLocalGroupHook_group_line");
		
		if (groupLine == null || groupLine.equals("")) {
			groupLine = "http_callback_line_1";
		}

		String groupSplit = appConfig.getConfigValue("UserStoreLocalGroupHook_group_split");
		
		if (groupSplit == null || groupSplit.equals("")) {
			groupSplit = ";";
		}

		String serviceConnect = appConfig.getConfigValue("UserStoreLocalGroupHook_service_connect");

		String groupsString = user.getGenericStore().get(groupLine);
		Set<String> groups = createGroupSet(groupsString, groupSplit);

		String groupsStringOld = user.getGenericStore().get(groupLine + "_old");
		Set<String> groupsOld = createGroupSet(groupsStringOld, groupSplit);

		Set<String> groupsToAdd = new HashSet<String>();
		groupsToAdd.addAll(groups);
		groupsToAdd.removeAll(groupsOld);
		
		Set<String> groupsToRemove = new HashSet<String>();
		groupsToRemove.addAll(groupsOld);
		groupsToRemove.removeAll(groups);
		
		for (String groupString : groupsToAdd) {
			LocalGroupEntity group = localGroupDao.findByName(groupString);
			if (group == null) {
				logger.debug("Creating group {}", groupString);

				Integer gidNumber = getGidNumber();
				if (gidNumber == null)
					continue;
				
				group = localGroupDao.createNew();
				group.setGidNumber(gidNumber);
				group.setName(groupString);
				group.setUsers(new HashSet<UserGroupEntity>());
				group = localGroupDao.persist(group);

				if (serviceConnect != null && (! serviceConnect.equals(""))) {
					for (String sc : serviceConnect.split(";")) {
						createServiceGroupFlag(sc, group);
					}
				}
			}
			
			if (! groupDao.isUserInGroup(user, group)) {
				logger.debug("Adding user {} to group {}", user.getEppn(), groupString);
				groupDao.addUserToGroup(user, group);
				changedGroups.add(group);
			}
			
		}
		
		for (String groupString : groupsToRemove) {
			LocalGroupEntity group = localGroupDao.findByName(groupString);
			if (group != null && groupDao.isUserInGroup(user, group)) {
				logger.debug("Removing user {} from group {}", user.getEppn(), groupString);
				groupDao.removeUserGromGroup(user, group);
				changedGroups.add(group);
			}
		}
		
		long end = System.currentTimeMillis();
		logger.debug("UserStoreLocalGroupHook postUpdateSecondaryGroupFromAttribute for user {} to {}ms", user.getEppn(), (end - start));
	}

	private Set<String> createGroupSet(String groupsString, String split) {
		Set<String> groups = new HashSet<String>();

		if (groupsString == null || groupsString.equals(""))
			return groups;
		
		String[] groupsSplit = groupsString.split(split);
		for (String group : groupsSplit) {
			group = group.trim();
			group = group.toLowerCase();
			groups.add(group);
		}
		
		return groups;
	}
	
	@Override
	public boolean isPrimaryResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		return false;
	}

	@Override
	public boolean isPrimaryCompleteOverride() {
		return false;
	}

	@Override
	public boolean isSecondaryResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		
		String groupLine = appConfig.getConfigValue("UserStoreLocalGroupHook_group_line");
		
		if (groupLine == null || groupLine.equals("")) {
			groupLine = "http_callback_line_1";
		}
		
		if (user.getGenericStore().containsKey(groupLine))
			return true;
		else
			return false;
	}

	@Override
	public boolean isSecondaryCompleteOverride() {
		return false;
	}

	private Integer getGidNumber() {
		InitialContext ic;
		try {
			ic = new InitialContext();
			SerialService serialService = (SerialService) ic.lookup("global/bwreg/bwreg-service/SerialServiceImpl!edu.kit.scc.webreg.service.SerialService");
			return serialService.next("gid-number-serial").intValue();
		} catch (NamingException e) {
			logger.warn("Error happened", e);
			return null;
		}
	}
	
	private void createServiceGroupFlag(String serviceShortName, ServiceBasedGroupEntity groupEntity) {
		InitialContext ic;
		try {
			ic = new InitialContext();
			ServiceService serviceService = (ServiceService) ic.lookup("global/bwreg/bwreg-service/ServiceServiceImpl!edu.kit.scc.webreg.service.ServiceService");
			ServiceGroupFlagService groupFlagService = (ServiceGroupFlagService) ic.lookup("global/bwreg/bwreg-service/ServiceGroupFlagServiceImpl!edu.kit.scc.webreg.service.ServiceGroupFlagService");
			
			ServiceEntity serviceEntity = serviceService.findByShortName(serviceShortName);
			if (serviceEntity != null) {
				ServiceGroupFlagEntity groupFlag = groupFlagService.createNew();
				groupFlag.setService(serviceEntity);
				groupFlag.setGroup(groupEntity);
				groupFlag.setStatus(ServiceGroupStatus.CLEAN);
				
				groupFlag = groupFlagService.save(groupFlag);
			}
		} catch (NamingException e) {
			logger.warn("Error happened", e);
		}
	}
}
