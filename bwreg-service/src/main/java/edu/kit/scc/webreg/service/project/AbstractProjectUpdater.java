package edu.kit.scc.webreg.service.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;

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
				getDao().deleteMembership(membership);
				for (UserEntity user : memberToRemove.getUsers()) {
					groupDao.removeUserGromGroup(user, project.getProjectGroup());
				}
			}
		}
		
		for (IdentityEntity memberToAdd : membersToAdd) {
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

		for (ProjectMembershipEntity pme : memberList) {
			syncMemberToGroup(project, pme.getIdentity(), executor);
		}
	}
	
	public void syncMemberToGroup(ProjectEntity project, IdentityEntity identity, String executor) {
		Set<ProjectServiceEntity> pseList = getDao().findServicesForProject(project, true);

		for (ProjectServiceEntity pse : pseList) {
			
			RegistryEntity registry = registryDao.findByServiceAndIdentityAndStatus(pse.getService(), identity, RegistryStatus.ACTIVE);
			if (registry == null) {
				registry = registryDao.findByServiceAndIdentityAndStatus(pse.getService(), identity, RegistryStatus.LOST_ACCESS);
			}

			if (registry != null) {
				/*
				 * if user is registered for service, also add it to group member list
				 */
				if (! groupDao.isUserInGroup(registry.getUser(), project.getProjectGroup())) {
					groupDao.addUserToGroup(registry.getUser(), project.getProjectGroup());
				}
			}
		}		
	}

	public void updateServices(ProjectEntity project, Set<ServiceEntity> serviceList, String executor) {
		project = getDao().findById(project.getId());
		updateServices(project, serviceList, executor, 0, 3);
	}
	
	private void updateServices(ProjectEntity project, Set<ServiceEntity> serviceList, String executor, int depth, int maxDepth) {
		if (depth >= maxDepth) {
			return;
		}

		Set<ProjectServiceEntity> oldServiceList = getDao().findServicesForProject(project, true);
		List<ServiceEntity> newServiceList = new ArrayList<ServiceEntity>(serviceList);

		for (ProjectServiceEntity oldService : oldServiceList) {
			if (! serviceList.contains(oldService.getService())) {
				if (depth == 0) {
					// only change services for directly called project, not for children
					getDao().deleteProjectService(oldService);
				}
				List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByGroupAndService(project.getProjectGroup(), oldService.getService());
				for (ServiceGroupFlagEntity groupFlag : groupFlagList) {
					groupFlag.setStatus(ServiceGroupStatus.TO_DELETE);
				}
			}
			else {
				newServiceList.remove(oldService.getService());
			}
		}
		
		for (ServiceEntity newService : newServiceList) {
			if (depth == 0) {
				// only change services for directly called project, not for children
				getDao().addServiceToProject(project, newService, ProjectServiceType.PASSIVE_GROUP);
			}
			ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
			groupFlag.setGroup(project.getProjectGroup());
			groupFlag.setService(newService);
			groupFlag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlag = groupFlagDao.persist(groupFlag);
		}
		
		triggerGroupUpdate(project, executor);
		
		for (ProjectEntity childProject : project.getChildProjects()) {
			updateServices(childProject, serviceList, executor, depth + 1, maxDepth);
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
