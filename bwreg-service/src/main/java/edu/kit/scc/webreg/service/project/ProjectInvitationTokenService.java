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

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.service.BaseService;

public interface ProjectInvitationTokenService extends BaseService<ProjectInvitationTokenEntity> {

	ProjectInvitationTokenEntity sendEmailToken(ProjectEntity project, IdentityEntity identity, String rcptMail,
			String rcptName, String senderName, String customMessage, String executor);

	ProjectInvitationTokenEntity acceptEmailToken(ProjectInvitationTokenEntity token, String executor);

	ProjectInvitationTokenEntity declineEmailToken(ProjectInvitationTokenEntity token, String executor);


}
