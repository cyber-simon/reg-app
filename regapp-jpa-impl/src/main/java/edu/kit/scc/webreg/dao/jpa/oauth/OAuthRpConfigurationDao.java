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
package edu.kit.scc.webreg.dao.jpa.oauth;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.oauth.OAuthRpConfigurationEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class OAuthRpConfigurationDao extends JpaBaseDao<OAuthRpConfigurationEntity> {

	@Override
	public Class<OAuthRpConfigurationEntity> getEntityClass() {
		return OAuthRpConfigurationEntity.class;
	}

}
