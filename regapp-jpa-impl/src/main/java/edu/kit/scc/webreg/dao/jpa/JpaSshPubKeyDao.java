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
package edu.kit.scc.webreg.dao.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;

@Named
@ApplicationScoped
public class JpaSshPubKeyDao extends JpaBaseDao<SshPubKeyEntity> implements SshPubKeyDao {

	@Override
	public Class<SshPubKeyEntity> getEntityClass() {
		return SshPubKeyEntity.class;
	}

}
