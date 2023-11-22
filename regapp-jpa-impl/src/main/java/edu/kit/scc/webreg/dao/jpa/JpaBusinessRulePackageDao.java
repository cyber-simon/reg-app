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

import edu.kit.scc.webreg.dao.BusinessRulePackageDao;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;

@Named
@ApplicationScoped
public class JpaBusinessRulePackageDao extends JpaBaseDao<BusinessRulePackageEntity> implements BusinessRulePackageDao {

	@Override
	public Class<BusinessRulePackageEntity> getEntityClass() {
		return BusinessRulePackageEntity.class;
	}

}
