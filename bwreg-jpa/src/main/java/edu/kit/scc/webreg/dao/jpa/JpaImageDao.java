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
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ImageDao;
import edu.kit.scc.webreg.entity.ImageEntity;

@Named
@ApplicationScoped
public class JpaImageDao extends JpaBaseDao<ImageEntity, Long> implements ImageDao {

    @Override
	public ImageEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ImageEntity> criteria = builder.createQuery(ImageEntity.class);
		Root<ImageEntity> root = criteria.from(ImageEntity.class);
		criteria.where(
				builder.equal(root.get("name"), name));
		criteria.select(root);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public ImageEntity findByIdWithData(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ImageEntity> criteria = builder.createQuery(ImageEntity.class);
		Root<ImageEntity> root = criteria.from(ImageEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("imageData");

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	@Override
	public Class<ImageEntity> getEntityClass() {
		return ImageEntity.class;
	}
}
