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
package edu.kit.scc.webreg.dao.jpa.project;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.project.LocalProjectDao;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;

@Named
@ApplicationScoped
public class JpaLocalProjectDao extends JpaBaseProjectDao<LocalProjectEntity> implements LocalProjectDao {

	@Override
	public Class<LocalProjectEntity> getEntityClass() {
		return LocalProjectEntity.class;
	}

}
