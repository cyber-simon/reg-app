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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.project.ExternalOidcProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;

@Named
@ApplicationScoped
public class JpaExternalOidcProjectDao extends JpaExternalProjectDao<ExternalOidcProjectEntity> implements ExternalOidcProjectDao {

	@Override
	public ExternalOidcProjectEntity findByExternalNameOidc(String externalName, OidcRpConfigurationEntity rpConfig) {
		try {
			return (ExternalOidcProjectEntity) em.createQuery("select r from ExternalOidcProjectEntity r "
					+ "where r.externalName = :externalName and r.rpConfig = :rpConfig order by r.name")
					.setParameter("externalName", externalName)
					.setParameter("rpConfig", rpConfig)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExternalOidcProjectEntity> findByService(ServiceEntity service) {
		return em.createQuery("select r.project from ExternalOidcProjectEntity r where r.service = :service order by r.project.name")
			.setParameter("service", service).getResultList();
	}

	@Override
	public Class<ExternalOidcProjectEntity> getEntityClass() {
		return ExternalOidcProjectEntity.class;
	}
}
