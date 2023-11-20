package edu.kit.scc.webreg.service.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.project.LocalProjectDao;
import edu.kit.scc.webreg.dao.project.LocalProjectGroupDao;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectGroupEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;

@ApplicationScoped
public class LocalProjectCreater extends AbstractProjectCreater<LocalProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private LocalProjectDao dao;

	@Inject
	private LocalProjectGroupDao projectGroupDao;
	
	@Inject
	private GroupDao groupDao;

	public LocalProjectEntity create(LocalProjectEntity project, IdentityEntity identity) {

		LocalProjectGroupEntity projectGroup = project.getProjectGroup();
		if (projectGroup == null) {
			projectGroup = projectGroupDao.createNew();
			projectGroup.setName(project.getGroupName());
			projectGroup.setGidNumber(groupDao.getNextGID().intValue());
			projectGroup = projectGroupDao.persist(projectGroup);
			project.setProjectGroup(projectGroup);
		}

		project = dao.persist(project);
		dao.addAdminToProject(project, identity, ProjectAdminType.OWNER);

		return project;
	}
}
