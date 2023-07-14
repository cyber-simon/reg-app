package edu.kit.scc.webreg.service.project;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.project.AttributeSourceProjectDao;
import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;

@ApplicationScoped
public class AttributeSourceProjectUpdater extends AbstractProjectUpdater<AttributeSourceProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeSourceProjectDao dao;

	@Inject
	private AttributeSourceProjectCreater projectCreater;

	public boolean syncAttributeSourceProjects(List<String> externalNamesList, UserEntity user,
			AttributeSourceEntity attributeSource) {
		boolean changed = false;

		IdentityEntity identity = user.getIdentity();

		List<ProjectIdentityAdminEntity> adminList = dao.findAdminByIdentity(identity);
		// TODO move this logic to dao query
		List<ProjectIdentityAdminEntity> filteredAdminList = adminList.stream()
				.filter(pia -> (pia.getProject() instanceof AttributeSourceProjectEntity)
						&& (((AttributeSourceProjectEntity) pia.getProject()).getAttributeSource()
								.equals(attributeSource)))
				.collect(Collectors.toList());

		logger.debug("Identity {} is admin in {} projects and {} in attribute sourced proects", identity.getId(),
				adminList.size(), filteredAdminList.size());

		for (ProjectIdentityAdminEntity pia : filteredAdminList) {
			if (!externalNamesList.contains(((AttributeSourceProjectEntity) pia.getProject()).getExternalName())) {
				logger.debug("Project {} no longer in external attribute source. Removing ProjectIdentityAdminEntity", pia.getProject().getId());
				dao.removeAdminFromProject(pia);
				changed = true;
			}
		}

		for (String name : externalNamesList) {
			if (filteredAdminList.stream()
					.filter(pia -> ((AttributeSourceProjectEntity) pia.getProject()).getExternalName().equals(name))
					.findFirst().isEmpty()) {
				logger.debug("Project {} not in ProjectIdentityAdminEntity for identity {}", name, identity.getId());
				
				AttributeSourceProjectEntity project = dao.findByExternalNameAttributeSource(name, attributeSource);
				if (project == null) {
					project = projectCreater.create(name, name, name, name, attributeSource);
				}
				
				dao.addAdminToProject(project, identity, ProjectAdminType.OWNER);
			}
		}

		return changed;
	}
/*
	public void syncExternalOidcProject(String projectName, String externalName, String groupName, String shortName,
			UserEntity user, AttributeSourceEntity attributeSource) {

		logger.debug("Inspecting {}", projectName);
		AttributeSourceProjectEntity project = dao.findByExternalNameAttributeSource(externalName, attributeSource);

		if (shortName == null) {
			// generate short name, if none is set
			shortName = "p_" + (UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24));
		}

		if (project == null) {
			project = projectCreater.create(projectName, externalName, groupName, shortName, attributeSource);
		}

		project.setName(projectName);
		project.setGroupName(groupName);
		project.getProjectGroup().setName(groupName);
		project.setShortName(shortName);
		project.setAttributeSource(attributeSource);

		if (dao.findByIdentityAndProject(user.getIdentity(), project) == null) {
			dao.addMemberToProject(project, user.getIdentity(), ProjectMembershipType.MEMBER);
		}

		syncAllMembersToGroup(project, "idty-" + user.getIdentity());
		triggerGroupUpdate(project, "idty-" + user.getIdentity());
	}
*/
	@Override
	protected BaseProjectDao<AttributeSourceProjectEntity> getDao() {
		return dao;
	}
}
