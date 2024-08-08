package edu.kit.scc.webreg.service.project;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceStatusType;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;
import edu.kit.scc.webreg.entity.project.ProjectStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.event.ProjectMembershipEvent;
import edu.kit.scc.webreg.event.ProjectServiceEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import jakarta.inject.Inject;

public abstract class AbstractProjectUpdater<T extends ProjectEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private GroupDao groupDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;

	@Inject
	private EventSubmitter eventSubmitter;

	protected abstract BaseProjectDao<T> getDao();

	public void approve(ProjectServiceEntity pse, String executor) {
		if (!pse.getStatus().equals(ProjectServiceStatusType.APPROVAL_PENDING)) {
			return;
		}

		ProjectServiceEvent event = new ProjectServiceEvent(pse);
		try {
			eventSubmitter.submit(event, EventType.PROJECT_SERVICE_APPROVAL, executor, false);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		pse.setStatus(ProjectServiceStatusType.ACTIVE);
		syncGroupFlags(pse, executor);
		triggerGroupUpdate(pse.getProject(), executor);
	}

	public void deny(ProjectServiceEntity pse, String denyMessage, String executor) {
		if (!pse.getStatus().equals(ProjectServiceStatusType.APPROVAL_PENDING)) {
			return;
		}

		ProjectServiceEvent event = new ProjectServiceEvent(pse);
		try {
			eventSubmitter.submit(event, EventType.PROJECT_SERVICE_DENIED, executor, false);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		pse.setStatus(ProjectServiceStatusType.APPROVAL_DENIED);
	}

	public void deleteProject(ProjectEntity project, String executor) {
		for (ProjectServiceEntity pse : project.getProjectServices()) {
			logger.debug("Setting ProjectServiceStatus to DELETED for Project {} and Service {}", project.getName(), pse.getService().getName());
			addOrChangeService(project, pse.getService(), pse.getType(), ProjectServiceStatusType.DELETED, executor);
		}
		
		for (ProjectIdentityAdminEntity pia : project.getProjectAdmins()) {
			logger.debug("Removing project admin {} ({}) from project {}", pia.getIdentity().getId(), pia.getType(), project.getName());
			getDao().removeAdminFromProject(pia);
		}
		
		updateProjectMemberList(project, new HashSet<>(), executor);
		String rnd = randomAlphanumeric(16).toLowerCase();
		project.setShortName("deleted-" + rnd);
		project.setGroupName("deleted-" + rnd);
		project.setProjectStatus(ProjectStatus.DELETED);
	}
	
	public void updateGroupnameOverride(ProjectServiceEntity pse, String overrideGroupname, String executor) {
		pse.setGroupNameOverride(overrideGroupname);
		syncGroupFlags(pse, executor);
		triggerGroupUpdate(pse.getProject(), executor);
	}

	public void removeProjectMember(ProjectMembershipEntity pme, String executor) {
		ProjectEntity project = getDao().fetch(pme.getProject().getId());
		logger.debug("Remove member {} from project {}", pme.getIdentity().getId(), project.getId());
		getDao().removeMemberFromProject(pme);

		ProjectMembershipEvent event = new ProjectMembershipEvent(pme);
		try {
			eventSubmitter.submit(event, EventType.PROJECT_MEMBER_REMOVED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		updateProjectMemberList(project, executor, 0, 3);
	}

	public void addProjectMember(ProjectEntity project, IdentityEntity identity, String executor) {
		logger.debug("Adding member {} to project {}", identity.getId(), project.getId());
		ProjectMembershipEntity pme = getDao().addMemberToProject(project, identity, ProjectMembershipType.MEMBER);

		ProjectMembershipEvent event = new ProjectMembershipEvent(pme);
		try {
			eventSubmitter.submit(event, EventType.PROJECT_MEMBER_ADD, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		updateProjectMemberList(project, executor, 0, 3);
	}

	public void updateProjectMemberList(ProjectEntity project, Set<IdentityEntity> memberList, String executor) {
		// calculate member difference lists, for propagation to parent projects
		List<IdentityEntity> oldMemberList = getDao().findIdentitiesForProject(project);
		List<IdentityEntity> newMemberList = new ArrayList<IdentityEntity>(memberList);

		List<IdentityEntity> membersToRemove = new ArrayList<IdentityEntity>(oldMemberList);
		membersToRemove.removeAll(newMemberList);

		List<IdentityEntity> membersToAdd = new ArrayList<IdentityEntity>(newMemberList);
		membersToAdd.removeAll(oldMemberList);

		for (IdentityEntity memberToRemove : membersToRemove) {
			ProjectMembershipEntity membership = getDao().findByIdentityAndProject(memberToRemove, project);
			if (membership != null) {
				// if membership is null, identity is not in project
				logger.debug("Delete member {} from project {}", memberToRemove.getId(), project.getId());
				getDao().deleteMembership(membership);
				for (UserEntity user : memberToRemove.getUsers()) {
					groupDao.removeUserGromGroup(user, project.getProjectGroup());
				}
			}
		}

		for (IdentityEntity memberToAdd : membersToAdd) {
			logger.debug("Adding member {} to project {}", memberToAdd.getId(), project.getId());
			getDao().addMemberToProject(project, memberToAdd, ProjectMembershipType.MEMBER);
		}

		updateProjectMemberList(project, executor, 0, 3);
	}

	private void updateProjectMemberList(ProjectEntity project, String executor, int depth, int maxDepth) {
		if (depth >= maxDepth) {
			return;
		}

		syncAllMembersToGroup(project, executor);
		triggerGroupUpdate(project, executor);

		if (project.getParentProject() != null) {
			updateProjectMemberList(project.getParentProject(), executor, 0, 3);
		}
	}

	public void syncAllMembersToGroup(ProjectEntity project, String executor) {
		List<ProjectMembershipEntity> memberList = getDao().findMembersForProject(project, true);

		Set<UserGroupEntity> ugs = project.getProjectGroup().getUsers();
		Set<UserEntity> users = new HashSet<>(ugs.stream().map(ug -> ug.getUser()).toList());
		
		// Sync all members to group
		for (ProjectMembershipEntity pme : memberList) {
			syncMemberToGroup(project, pme.getIdentity(), executor);
			for (UserEntity user : pme.getIdentity().getUsers()) {
				users.remove(user);
			}
		}
		
		for (UserEntity user : users) {
			logger.info("Remove user {} from project-group for project {}", user.getId(), project.getName());
			groupDao.removeUserGromGroup(user, project.getProjectGroup());
		}
	}

	public void syncMemberToGroup(ProjectEntity project, IdentityEntity identity, String executor) {
		for (UserEntity user : identity.getUsers()) {
			/*
			 * add all users from identity to project group
			 */
			if (!groupDao.isUserInGroup(user, project.getProjectGroup())) {
				groupDao.addUserToGroup(user, project.getProjectGroup());
			}
		}
	}

	public void addOrChangeService(ProjectEntity project, ServiceEntity service, ProjectServiceType type,
			ProjectServiceStatusType status, String executor) {
		project = getDao().fetch(project.getId());
		ProjectServiceEntity pse = getDao().findByServiceAndProject(service, project);

		if (pse != null) {
			logger.info("Found an entry for project {} and service {}, changing", project.getName(), service.getName());
			pse.setStatus(status);
			pse.setType(type);
		} else {
			pse = getDao().addServiceToProject(project, service, type, status);
			logger.info("Added new ProjectService connection for project {} and service {}", project.getName(),
					service.getName());
		}

		ProjectServiceEvent event = new ProjectServiceEvent(pse);
		try {
			if (ProjectServiceStatusType.APPROVAL_PENDING.equals(status))
				eventSubmitter.submit(event, EventType.PROJECT_SERVICE_APPROVAL, executor, false);
			else if (ProjectServiceStatusType.APPROVAL_DENIED.equals(status))
				eventSubmitter.submit(event, EventType.PROJECT_SERVICE_DENIED, executor, false);
			else if (ProjectServiceStatusType.ACTIVE.equals(status))
				eventSubmitter.submit(event, EventType.PROJECT_SERVICE_APPROVED, executor, false);
			else if (ProjectServiceStatusType.DELETED.equals(status))
				eventSubmitter.submit(event, EventType.PROJECT_SERVICE_DELETED, executor, false);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		syncGroupFlags(pse, executor);
		syncAllMembersToGroup(pse.getProject(), executor);
		triggerGroupUpdate(pse.getProject(), executor);
	}

	public void updateServices(ProjectEntity project, Set<ServiceEntity> serviceList, ProjectServiceType type,
			ProjectServiceStatusType status, String executor) {
		project = getDao().fetch(project.getId());
		updateServices(project, serviceList, type, status, executor, 0, 3);
	}

	private void updateServices(ProjectEntity project, Set<ServiceEntity> serviceList, ProjectServiceType type,
			ProjectServiceStatusType status, String executor, int depth, int maxDepth) {
		if (depth >= maxDepth) {
			return;
		}

		Set<ProjectServiceEntity> actualServices = getDao().findServicesForProject(project, false);
		Set<ProjectServiceEntity> actualParentServices;
		if (project.getParentProject() != null) {
			actualParentServices = getDao().findServicesForProject(project.getParentProject(), true);
		} else {
			actualParentServices = new HashSet<ProjectServiceEntity>();
		}

		List<ServiceEntity> actualServiceList = new ArrayList<ServiceEntity>(actualServices.size());
		actualServices.stream().forEach(o -> actualServiceList.add(o.getService()));
		List<ServiceEntity> actualParentServiceList = new ArrayList<ServiceEntity>(actualParentServices.size());
		actualParentServices.stream().forEach(o -> actualParentServiceList.add(o.getService()));

		// only change services for directly called project, not for children
		if (depth == 0) {
			List<ServiceEntity> newServiceList = new ArrayList<ServiceEntity>(serviceList);

			for (ProjectServiceEntity oldService : actualServices) {
				if (!serviceList.contains(oldService.getService())) {
					getDao().deleteProjectService(oldService);
				}
				newServiceList.remove(oldService.getService());
			}

			for (ServiceEntity newService : newServiceList) {
				getDao().addServiceToProject(project, newService, type, status);
			}
		}

		Set<ServiceEntity> allServiceList = new HashSet<ServiceEntity>(serviceList);
		allServiceList.addAll(actualParentServiceList);
		if (depth > 0) {
			// we are handling child group
			// that means, we also need to add our own services,
			// if not, our own services would be dropped
			allServiceList.addAll(actualServiceList);
		}

		syncGroupFlags(project, allServiceList);

		triggerGroupUpdate(project, executor);

		if (project.getChildProjects() != null) {
			for (ProjectEntity childProject : project.getChildProjects()) {
				updateServices(childProject, serviceList, type, status, executor, depth + 1, maxDepth);
			}
		}
	}

	private void syncGroupFlags(ProjectServiceEntity pse, String executor) {
		List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao
				.findByGroupAndService(pse.getProject().getProjectGroup(), pse.getService());

		if (groupFlagList.size() > 1) {
			logger.warn(
					"There are more than one GroupFlag for Project {} and Service {}. There should be only one or zero.",
					pse.getProject().getName(), pse.getService().getName());
		}

		if (pse.getStatus() != ProjectServiceStatusType.ACTIVE) {
			// PSE is not active, delete all groups, if there are any
			for (ServiceGroupFlagEntity gf : groupFlagList) {
				gf.setStatus(ServiceGroupStatus.TO_DELETE);
			}
		} else {
			// PSE is ACTIVE, create group flags if missing
			if (groupFlagList.size() == 0) {
				ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
				groupFlag.setGroup(pse.getProject().getProjectGroup());
				groupFlag.setService(pse.getService());
				groupFlag.setStatus(ServiceGroupStatus.DIRTY);
				groupFlag.setGroupNameOverride(pse.getGroupNameOverride());
				groupFlag = groupFlagDao.persist(groupFlag);
			} else if (groupFlagList.size() == 1) {
				ServiceGroupFlagEntity groupFlag = groupFlagList.get(0);
				if (pse.getGroupNameOverride() != null
						&& !(pse.getGroupNameOverride().equals(groupFlag.getGroupNameOverride()))) {
					// Group override names are different. Update the group flag one
					groupFlag.setStatus(ServiceGroupStatus.DIRTY);
					groupFlag.setGroupNameOverride(pse.getGroupNameOverride());
				}
			}
		}
	}

	private void syncGroupFlags(ProjectEntity project, Set<ServiceEntity> allServiceList) {
		List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByGroup(project.getProjectGroup());
		List<ServiceEntity> groupFlagServiceList = new ArrayList<ServiceEntity>(groupFlagList.size());
		groupFlagList.stream().forEach(o -> groupFlagServiceList.add(o.getService()));

		Set<ServiceEntity> flagsToRemove = new HashSet<ServiceEntity>(groupFlagServiceList);
		flagsToRemove.removeAll(allServiceList);

		Set<ServiceEntity> flagsToAdd = new HashSet<ServiceEntity>(allServiceList);
		flagsToAdd.removeAll(groupFlagServiceList);

		for (ServiceEntity s : flagsToRemove) {
			List<ServiceGroupFlagEntity> gfl = groupFlagDao.findByGroupAndService(project.getProjectGroup(), s);
			for (ServiceGroupFlagEntity gf : gfl) {
				gf.setStatus(ServiceGroupStatus.TO_DELETE);
			}
		}

		for (ServiceEntity s : flagsToAdd) {
			ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
			groupFlag.setGroup(project.getProjectGroup());
			groupFlag.setService(s);
			groupFlag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlag = groupFlagDao.persist(groupFlag);
		}

	}

	public void triggerGroupUpdate(ProjectEntity project, String executor) {
		List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByGroup(project.getProjectGroup());
		for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
			if (ServiceGroupStatus.CLEAN.equals(groupFlag.getStatus())) {
				groupFlag.setStatus(ServiceGroupStatus.DIRTY);
			}
		}

		HashSet<GroupEntity> userGroups = new HashSet<GroupEntity>();
		userGroups.add(project.getProjectGroup());
		MultipleGroupEvent mge = new MultipleGroupEvent(userGroups);
		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
	}
}
