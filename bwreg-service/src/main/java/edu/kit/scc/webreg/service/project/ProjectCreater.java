package edu.kit.scc.webreg.service.project;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.project.LocalProjectGroupDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectGroupEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;

@ApplicationScoped
public class ProjectCreater {

	@Inject
	private Logger logger;
	
	@Inject
	private ProjectDao projectDao;

	@Inject
	private LocalProjectGroupDao projectGroupDao;
	
	@Inject
	private GroupDao groupDao;

	public ProjectEntity create(ProjectEntity project, IdentityEntity identity) {

		LocalProjectGroupEntity projectGroup = project.getProjectGroup();
		if (projectGroup == null) {
			projectGroup = projectGroupDao.createNew();
			projectGroup.setName(project.getGroupName());
			projectGroup.setGidNumber(groupDao.getNextGID().intValue());
			projectGroup = projectGroupDao.persist(projectGroup);
			project.setProjectGroup(projectGroup);
		}

		project = projectDao.persist(project);
		projectDao.addAdminToProject(project, identity, ProjectAdminType.OWNER);

		return project;
	}
	
	public ExternalOidcProjectEntity createExternalOidcProject(String projectName, String externalName, String groupName, String shortName) {
		ExternalOidcProjectEntity project = new ExternalOidcProjectEntity();
		
		LocalProjectGroupEntity projectGroup = projectGroupDao.createNew();
		projectGroup.setName(groupName);
		projectGroup.setGidNumber(groupDao.getNextGID().intValue());
		projectGroup = projectGroupDao.persist(projectGroup);
		project.setProjectGroup(projectGroup);
		
		project.setName(projectName);
		project.setGroupName(groupName);
		project.setExternalName(externalName);
		project.setShortName(shortName);
		
		project = (ExternalOidcProjectEntity) projectDao.persist(project);
		
		return project;
	}
}
