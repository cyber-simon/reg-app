package edu.kit.scc.webreg.service.project;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;

@ApplicationScoped
public class LocalProjectUpdater extends AbstractProjectUpdater<LocalProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityDao identityDao;
	
	



}
