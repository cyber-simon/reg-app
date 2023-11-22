package edu.kit.scc.webreg.service.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.dao.project.LocalProjectDao;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;

@ApplicationScoped
public class LocalProjectUpdater extends AbstractProjectUpdater<LocalProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private LocalProjectDao dao;
	
	@Override
	protected BaseProjectDao<LocalProjectEntity> getDao() {
		return dao;
	}
}
