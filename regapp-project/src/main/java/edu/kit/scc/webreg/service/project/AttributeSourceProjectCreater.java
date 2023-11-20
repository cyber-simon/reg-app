package edu.kit.scc.webreg.service.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.project.AttributeSourceProjectDao;
import edu.kit.scc.webreg.dao.project.LocalProjectGroupDao;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectGroupEntity;

@ApplicationScoped
public class AttributeSourceProjectCreater extends AbstractProjectCreater<AttributeSourceProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private AttributeSourceProjectDao dao;

	@Inject
	private LocalProjectGroupDao projectGroupDao;
	
	@Inject
	private GroupDao groupDao;

	public AttributeSourceProjectEntity create(String projectName, String externalName, String groupName, String shortName, AttributeSourceEntity attributeSource) {
		AttributeSourceProjectEntity project = new AttributeSourceProjectEntity();
		
		LocalProjectGroupEntity projectGroup = projectGroupDao.createNew();
		projectGroup.setName(groupName);
		projectGroup.setGidNumber(groupDao.getNextGID().intValue());
		projectGroup = projectGroupDao.persist(projectGroup);
		project.setProjectGroup(projectGroup);
		
		project.setName(projectName);
		project.setGroupName(groupName);
		project.setExternalName(externalName);
		project.setShortName(shortName);
		project.setAttributeSource(attributeSource);
		
		project = (AttributeSourceProjectEntity) dao.persist(project);
		
		return project;
	}
}
