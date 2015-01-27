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

import edu.kit.scc.webreg.dao.AgreementTextDao;
import edu.kit.scc.webreg.entity.AgreementTextEntity;

@Named
@ApplicationScoped
public class JpaAgreementTextDao extends JpaBaseDao<AgreementTextEntity, Long> implements AgreementTextDao {

    @Override
	public Class<AgreementTextEntity> getEntityClass() {
		return AgreementTextEntity.class;
	}
}
