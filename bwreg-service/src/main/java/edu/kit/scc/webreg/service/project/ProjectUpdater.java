package edu.kit.scc.webreg.service.project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.project.LocalProjectGroupDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;

@ApplicationScoped
public class ProjectUpdater {

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityDao identityDao;
	
	@Inject
	private ProjectDao dao;
	
	@Inject
	private LocalProjectGroupDao projectGroupDao;
	
	@Inject
	private GroupDao groupDao;

	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private ServiceGroupFlagDao groupFlagDao;
	
	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ProjectCreater projectCreater;
	
	public void syncExternalOidcProject(String projectName, String externalName, String groupName, String shortName, OidcUserEntity user) {
		OidcRpConfigurationEntity rpConfig = user.getIssuer();
		
		logger.debug("Inspecting {}", projectName);
		ExternalOidcProjectEntity project = dao.findByExternalNameOidc(externalName, rpConfig);
		
		if (shortName == null) {
			// generate short name, if none is set
			shortName = "p_" + (UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24));
		}
		
		if (project == null) {
			project = projectCreater.createExternalOidcProject(projectName, externalName, groupName, shortName);
		}

		project.setName(projectName);
		project.setGroupName(groupName);
		project.getProjectGroup().setName(groupName);
		project.setShortName(shortName);
		project.setRpConfig(rpConfig);
		
		if (dao.findByIdentityAndProject(user.getIdentity(), project) == null) {
			dao.addMemberToProject(project, user.getIdentity(), ProjectMembershipType.MEMBER);
		}
		
		syncAllMembersToGroup(project, "idty-" + user.getIdentity());
		triggerGroupUpdate(project, "idty-" + user.getIdentity());
	}
	
	public void updateProjectMemberList(ProjectEntity project, Set<IdentityEntity> memberList, String executor) {
		project = dao.merge(project);
		
		// Save members first
		List<ProjectMembershipEntity> oldMemberList = dao.findMembersForProject(project);
		List<IdentityEntity> newMemberList = new ArrayList<IdentityEntity>(memberList);
		
		for (ProjectMembershipEntity oldMember : oldMemberList) {
			if (! memberList.contains(oldMember.getIdentity())) {
				dao.deleteMembership(oldMember);
				for (UserEntity user : oldMember.getIdentity().getUsers()) {
					groupDao.removeUserGromGroup(user, project.getProjectGroup());
				}
			}
			else {
				newMemberList.remove(oldMember.getIdentity());
			}
		}
		
		for (IdentityEntity newMember : newMemberList) {
			dao.addMemberToProject(project, newMember, ProjectMembershipType.MEMBER);
		}
		
		syncAllMembersToGroup(project, executor);
		triggerGroupUpdate(project, executor);
	}

	public void syncAllMembersToGroup(ProjectEntity project, String executor) {
		List<ProjectMembershipEntity> memberList = dao.findMembersForProject(project);

		for (ProjectMembershipEntity pme : memberList) {
			syncMemberToGroup(project, pme.getIdentity(), executor);
		}
	}
	
	public void syncMemberToGroup(ProjectEntity project, IdentityEntity identity, String executor) {
		List<ProjectServiceEntity> pseList = dao.findServicesForProject(project);

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
		List<ProjectServiceEntity> oldServiceList = dao.findServicesForProject(project);
		List<ServiceEntity> newServiceList = new ArrayList<ServiceEntity>(serviceList);

		for (ProjectServiceEntity oldService : oldServiceList) {
			if (! serviceList.contains(oldService.getService())) {
				dao.deleteProjectService(oldService);
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
			dao.addServiceToProject(project, newService, ProjectServiceType.PASSIVE_GROUP);
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
