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
package edu.kit.scc.webreg.dao.jpa.oidc;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpFlowStateDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpFlowStateEntity_;

@Named
@ApplicationScoped
public class JpaRpOidcFlowStateDao extends JpaBaseDao<OidcRpFlowStateEntity> implements OidcRpFlowStateDao {

	@Override
	public OidcRpFlowStateEntity findByState(String state) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcRpFlowStateEntity> criteria = builder.createQuery(OidcRpFlowStateEntity.class);
		Root<OidcRpFlowStateEntity> root = criteria.from(OidcRpFlowStateEntity.class);
		criteria.where(builder.equal(root.get(OidcRpFlowStateEntity_.state), state));
		criteria.select(root);
		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}	

	@Override
	public void deleteExpiredTokens() {
		Query query = em.createQuery("delete from OidcRpFlowStateEntity where validUntil <= :validUntil");
		query.setParameter("validUntil", new Date());
		query.executeUpdate();
	}
	
	@Override
	public Class<OidcRpFlowStateEntity> getEntityClass() {
		return OidcRpFlowStateEntity.class;
	}
}
