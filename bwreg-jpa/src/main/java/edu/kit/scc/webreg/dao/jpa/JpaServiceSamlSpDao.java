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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ServiceSamlSpDao;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;

@Named
@ApplicationScoped
public class JpaServiceSamlSpDao extends JpaBaseDao<ServiceSamlSpEntity, Long> implements ServiceSamlSpDao {

    @Override
	public List<ServiceSamlSpEntity> findByService(ServiceEntity service) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceSamlSpEntity> criteria = builder.createQuery(ServiceSamlSpEntity.class);
		Root<ServiceSamlSpEntity> root = criteria.from(ServiceSamlSpEntity.class);
		criteria.where(
				builder.equal(root.get("service"), service));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

    @Override
	public List<ServiceSamlSpEntity> findBySamlSpAndIdp(SamlIdpConfigurationEntity idp, SamlSpMetadataEntity sp) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceSamlSpEntity> criteria = builder.createQuery(ServiceSamlSpEntity.class);
		Root<ServiceSamlSpEntity> root = criteria.from(ServiceSamlSpEntity.class);
		criteria.where(
				builder.and(
						builder.equal(root.get("sp"), sp),
						builder.equal(root.get("idp"), idp)
				));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

    @Override
	public List<ServiceSamlSpEntity> findBySamlSp(SamlSpMetadataEntity sp) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceSamlSpEntity> criteria = builder.createQuery(ServiceSamlSpEntity.class);
		Root<ServiceSamlSpEntity> root = criteria.from(ServiceSamlSpEntity.class);
		criteria.where(
				builder.equal(root.get("sp"), sp));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

	@Override
	public Class<ServiceSamlSpEntity> getEntityClass() {
		return ServiceSamlSpEntity.class;
	}
}
