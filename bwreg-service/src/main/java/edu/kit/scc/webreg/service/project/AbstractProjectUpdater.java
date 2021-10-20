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
		
		// Save members first
		List<ProjectMembershipEntity> oldMemberList = getDao().findMembersForProject(project);
		List<IdentityEntity> newMemberList = new ArrayList<IdentityEntity>(memberList);
		
		for (ProjectMembershipEntity oldMember : oldMemberList) {
			if (! memberList.contains(oldMember.getIdentity())) {
				getDao().deleteMembership(oldMember);
				for (UserEntity user : oldMember.getIdentity().getUsers()) {
					groupDao.removeUserGromGroup(user, project.getProjectGroup());
				}
			}
			else {
				newMemberList.remove(oldMember.getIdentity());
			}
		}
		
		for (IdentityEntity newMember : newMemberList) {
			getDao().addMemberToProject(project, newMember, ProjectMembershipType.MEMBER);
		}
		
		syncAllMembersToGroup(project, executor);
		triggerGroupUpdate(project, executor);
	}

	public void syncAllMembersToGroup(ProjectEntity project, String executor) {
		List<ProjectMembershipEntity> memberList = getDao().findMembersForProject(project);

		for (ProjectMembershipEntity pme : memberList) {
			syncMemberToGroup(project, pme.getIdentity(), executor);
		}
	}
	
	public void syncMemberToGroup(ProjectEntity project, IdentityEntity identity, String executor) {
		List<ProjectServiceEntity> pseList = getDao().findServicesForProject(project);

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
		List<ProjectServiceEntity> oldServiceList = getDao().findServicesForProject(project);
		List<ServiceEntity> newServiceList = new ArrayList<ServiceEntity>(serviceList);

		for (ProjectServiceEntity oldService : oldServiceList) {
			if (! serviceList.contains(oldService.getService())) {
				getDao().deleteProjectService(oldService);
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
			getDao().addServiceToProject(project, newService, ProjectServiceType.PASSIVE_GROUP);
			ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
			groupFlag.setGroup(project.getProjectGroup());
			groupFlag.setService(newService);
			groupFlag.setStatus(ServiceGroupStatus.DIRTY);
			groupFlag = groupFlagDao.persist(groupFlag);
		}
		
		triggerGroupUpdate(project, executor);
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
