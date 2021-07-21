package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.oidc.OidcGroupDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.oidc.OidcGroupEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.saml.HomeIdResolver;

@ApplicationScoped
public class OidcGroupUpdater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private OidcGroupDao dao;
	
	@Inject
	private GroupDao groupDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;
	
	@Inject 
	private SerialService serialService;

	@Inject 
	private EventSubmitter eventSubmitter;

	@Inject
	private HomeIdResolver homeIdResolver;
	
	public Set<GroupEntity> updateGroupsForUser(OidcUserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		
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
		
		return changedGroups;
	}
	
	protected Set<GroupEntity> updatePrimary(OidcUserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		Set<GroupEntity> changedGroups = new HashSet<GroupEntity>();

		OidcGroupEntity group = null;
		
		String homeId = homeIdResolver.resolveHomeId(user, attributeMap);

		if (homeId == null) {
			logger.warn("No Home ID is set for User {}, resetting primary group", user.getId());
		}
		else {
			//Filter all non character from homeid
			homeId = homeId.toLowerCase();
			homeId = homeId.replaceAll("[^a-z0-9]", "");
			
			String groupName = homeId;
			
			logger.info("Setting standard HomeID group {} for user {}", homeId, user.getId());
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
				group.setIssuer(user.getIssuer());
				auditor.logAction(group.getName(), "SET FIELD", "oidcIssuer", "" + user.getIssuer().getName(), AuditStatus.SUCCESS);
				group = (OidcGroupEntity) groupDao.persistWithServiceFlags(group);
				auditor.logAction(group.getName(), "CREATE GROUP", null, "Group created", AuditStatus.SUCCESS);
				
				changedGroups.add(group);
			}
			
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
				changedGroups.add(group);
			}
		}
		else if (user.getPrimaryGroup() == null && group != null) {
			auditor.logAction(user.getEppn(), "UPDATE FIELD", "primaryGroup", 
					"null -> " + 
					group.getName() + " (" + group.getGidNumber() + ")", AuditStatus.SUCCESS);
			changedGroups.add(group);
		}

		user.setPrimaryGroup(group);

		return changedGroups;
	}
	
	protected Set<GroupEntity> updateSecondary(OidcUserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		Set<GroupEntity> changedGroups = new HashSet<GroupEntity>();

		/**
		 * TODO implement secondary groups for OIDC
		 * 
		 * if needed. Not sure, which attribute to take
		 */
		//String homeId = homeIdResolver.resolveHomeId(user, attributeMap);

		/*
		 * Add Primary group to secondary as well
		 */
		if (user.getPrimaryGroup() != null && (! groupDao.isUserInGroup(user, user.getPrimaryGroup()))) {
			logger.debug("Adding user {} to his primary group {} as secondary", user.getEppn(), user.getPrimaryGroup().getName());
			groupDao.addUserToGroup(user, user.getPrimaryGroup());
			changedGroups.add(user.getPrimaryGroup());
		}
		
		return changedGroups;
	}
}
