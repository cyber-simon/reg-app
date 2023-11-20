/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.project;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.project.ProjectInvitationTokenDao;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ProjectInvitationTokenServiceImpl extends BaseServiceImpl<ProjectInvitationTokenEntity>
		implements ProjectInvitationTokenService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ProjectInvitationTokenDao dao;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private ProjectInvitationTokenGenerator generator;

	@Override
	public ProjectInvitationTokenEntity sendEmailToken(ProjectEntity project, IdentityEntity identity, String rcptMail,
			String rcptName, String senderName, String customMessage, String executor) {
		return generator.sendToken(project, identity, rcptMail, rcptName, senderName, customMessage, executor);
	}

	@Override
	public ProjectInvitationTokenEntity acceptEmailToken(ProjectInvitationTokenEntity token, Long identityId,
			String executor) {
		token = dao.fetch(token.getId());
		IdentityEntity identity = identityDao.fetch(identityId);
		generator.acceptEmailToken(token, identity, executor);
		return token;
	}

	@Override
	public ProjectInvitationTokenEntity declineEmailToken(ProjectInvitationTokenEntity token, Long identityId,
			String executor) {
		token = dao.fetch(token.getId());
		IdentityEntity identity = identityDao.fetch(identityId);
		generator.declineEmailToken(token, identity, executor);
		return token;
	}

	@Override
	protected BaseDao<ProjectInvitationTokenEntity> getDao() {
		return dao;
	}
}
