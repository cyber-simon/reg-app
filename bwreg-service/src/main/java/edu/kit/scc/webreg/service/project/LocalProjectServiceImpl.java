/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.service.project;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.project.LocalProjectDao;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class LocalProjectServiceImpl extends BaseServiceImpl<LocalProjectEntity> implements LocalProjectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private LocalProjectDao dao;

	@Inject
	private LocalProjectCreater creater;

	@Inject
	private LocalProjectUpdater updater;

	@Override
	public LocalProjectEntity save(LocalProjectEntity project, Long identityId) {
		IdentityEntity identity = identityDao.fetch(identityId);
		return creater.create(project, identity);
	}

	@Override
	public void approve(ProjectServiceEntity pse, String executor) {
		pse = dao.mergeProjectService(pse);
		updater.approve(pse, executor);
	}

	@Override
	public void deleteProject(LocalProjectEntity project, String executor) {
		project = dao.fetch(project.getId());
		updater.deleteProject(project, executor);
	}

	@Override
	public void deny(ProjectServiceEntity pse, String denyMessage, String executor) {
		updater.deny(pse, denyMessage, executor);
	}

	@Override
	public void updateGroupnameOverride(ProjectServiceEntity pse, String overrideGroupname, String executor) {
		pse = dao.mergeProjectService(pse);
		updater.updateGroupnameOverride(pse, overrideGroupname, executor);
	}
	
	@Override
	protected BaseDao<LocalProjectEntity> getDao() {
		return dao;
	}

}
