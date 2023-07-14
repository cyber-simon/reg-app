package edu.kit.scc.webreg.service.project;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.project.AttributeSourceProjectDao;
import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;

@ApplicationScoped
public class AttributeSourceProjectUpdater extends AbstractProjectUpdater<AttributeSourceProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeSourceProjectDao dao;

	@Inject
	private AttributeSourceProjectCreater projectCreater;

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

	@Override
	protected BaseProjectDao<AttributeSourceProjectEntity> getDao() {
		return dao;
	}
}
