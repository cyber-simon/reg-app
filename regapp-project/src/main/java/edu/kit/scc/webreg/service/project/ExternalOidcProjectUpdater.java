package edu.kit.scc.webreg.service.project;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.dao.project.ExternalOidcProjectDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipType;

@ApplicationScoped
public class ExternalOidcProjectUpdater extends AbstractProjectUpdater<ExternalOidcProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ExternalOidcProjectDao dao;

	@Inject
	private ExternalOidcProjectCreater projectCreater;
	
	public void syncExternalOidcProject(String projectName, String externalName, String groupName, String shortName, OidcUserEntity user) {
		OidcRpConfigurationEntity rpConfig = user.getIssuer();
		
		logger.debug("Inspecting {}", projectName);
		ExternalOidcProjectEntity project = dao.findByExternalNameOidc(externalName, rpConfig);
		
		if (shortName == null) {
			// generate short name, if none is set
			shortName = "p_" + (UUID.randomUUID().toString().replaceAll("-", "").substring(0, 24));
		}
		
		if (project == null) {
			project = projectCreater.create(projectName, externalName, groupName, shortName, rpConfig);
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

	@Override
	protected BaseProjectDao<ExternalOidcProjectEntity> getDao() {
		return dao;
	}
}
