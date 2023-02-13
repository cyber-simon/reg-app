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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.ClusterMemberDao;
import edu.kit.scc.webreg.entity.ClusterMemberEntity;

@Named
@ApplicationScoped
public class JpaClusterMemberDao extends JpaBaseDao<ClusterMemberEntity> implements ClusterMemberDao {

	@Override
	public Class<ClusterMemberEntity> getEntityClass() {
		return ClusterMemberEntity.class;
	}

}
