package edu.kit.scc.webreg.service.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.project.ExternalOidcProjectDao;
import edu.kit.scc.webreg.dao.project.LocalProjectGroupDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectGroupEntity;

@ApplicationScoped
public class ExternalOidcProjectCreater extends AbstractProjectCreater<ExternalOidcProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ExternalOidcProjectDao dao;

	@Inject
	private LocalProjectGroupDao projectGroupDao;
	
	@Inject
	private GroupDao groupDao;

	public ExternalOidcProjectEntity create(String projectName, String externalName, String groupName, String shortName, OidcRpConfigurationEntity rpConfig) {
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
		project.setRpConfig(rpConfig);
		
		project = (ExternalOidcProjectEntity) dao.persist(project);
		
		return project;
	}
}
