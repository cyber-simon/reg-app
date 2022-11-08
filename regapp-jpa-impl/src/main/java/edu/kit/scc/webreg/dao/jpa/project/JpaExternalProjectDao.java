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
package edu.kit.scc.webreg.dao.jpa.project;

import java.util.List;

import edu.kit.scc.webreg.dao.project.ExternalProjectDao;
import edu.kit.scc.webreg.entity.project.ExternalProjectEntity;

public abstract class JpaExternalProjectDao<T extends ExternalProjectEntity> extends JpaBaseProjectDao<T> implements ExternalProjectDao<T> {

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findByExternalName(String externalName) {
		return em.createQuery("select r from ExternalProjectEntity r where r.externalName = :externalName order by r.name")
			.setParameter("externalName", externalName).getResultList();
	}
}
