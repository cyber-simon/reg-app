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

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SamlAuthnRequestDao;
import edu.kit.scc.webreg.entity.SamlAuthnRequestEntity;

@Named
@ApplicationScoped
public class JpaSamlAuthnRequestDao extends JpaBaseDao<SamlAuthnRequestEntity, Long> implements SamlAuthnRequestDao, Serializable {

	private static final long serialVersionUID = 1L;
    
	@Override
	public Class<SamlAuthnRequestEntity> getEntityClass() {
		return SamlAuthnRequestEntity.class;
	}
}
