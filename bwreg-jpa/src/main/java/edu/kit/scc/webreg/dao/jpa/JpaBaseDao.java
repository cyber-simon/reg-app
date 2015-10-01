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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.dao.ops.AndPredicate;
import edu.kit.scc.webreg.dao.ops.OrPredicate;
import edu.kit.scc.webreg.ds.DefaultDatasource;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class JpaBaseDao<T extends BaseEntity<PK>, PK extends Serializable> implements BaseDao<T, PK> {

	@Inject
	@DefaultDatasource
    protected EntityManager em;

    public abstract Class<T> getEntityClass(); 
    
	@Override
	public T createNew() {
		try {
			return getEntityClass().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public T persist(T entity) {
		entity = merge(entity);
		em.persist(entity);
		return entity;
	}

	@Override
	public T merge(T entity) {
		if (em.contains(entity))
			return entity;
		else
			return em.merge(entity);
	}

	@Override
	public void refresh(T entity) {
		em.refresh(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll() {
		return em.createQuery("select e from " + getEntityClass().getSimpleName() + " e").getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAllPaging(int first, int pageSize, String sortField,
			GenericSortOrder sortOrder, Map<String, Object> filterMap) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> root = criteria.from(getEntityClass());
		
		List<Predicate> predicates = predicatesFromFilterMap(builder, root, filterMap);
		criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		
		criteria.select(root);

		if (sortField != null && sortOrder != null && sortOrder != GenericSortOrder.NONE) {
			criteria.orderBy(getSortOrder(builder, root, sortField, sortOrder));
		}
		
		Query q = em.createQuery(criteria);
		q.setFirstResult(first).setMaxResults(pageSize);
		
		return q.getResultList();
	}

	@Override
	public Number countAll(Map<String, Object> filterMap) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<T> root = criteria.from(getEntityClass());
		
		List<Predicate> predicates = predicatesFromFilterMap(builder, root, filterMap);
		
		criteria.select(builder.count(root));
		criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
		
		TypedQuery<Long> q = em.createQuery(criteria);
		return q.getSingleResult();
	}
	
	@Override
	public T findById(PK id) {
		return em.find(getEntityClass(), id);
	}

	@Override
	public void delete(T entity) {
		entity = merge(entity);
		em.remove(entity);
	}
	
	@Override
	public boolean isPersisted(T entity) {
		return em.contains(entity);
	}
	
	@Override
	public T findByIdWithAttrs(PK id, String... attrs) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		criteria.where(builder.and(
				builder.equal(entity.get("id"), id)
				));
		criteria.select(entity);
		criteria.distinct(true);
		
		for (String attr : attrs)
			entity.fetch(attr, JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	
	
	protected List<Predicate> predicatesFromFilterMap(CriteriaBuilder builder, Root<T> root, Map<String, Object> filterMap) {
		
		List<Predicate> predicates = new ArrayList<Predicate>(filterMap.size());
		for (Entry<String, Object> entry : filterMap.entrySet()) {
			
			predicates.add(predicateFromObject(builder, root, entry.getKey(), entry.getValue()));
		}
		
		return predicates;
		
	}
	
	protected Predicate predicateFromObject(CriteriaBuilder builder, Root<T> root, String path, Object o) {
		if (o instanceof OrPredicate) {
			OrPredicate p = (OrPredicate) o;
			List<Predicate> pList = new ArrayList<Predicate>();
			for (Object object : p.getOperandList()) {
				pList.add(predicateFromObject(builder, root, path, object));
			}
			return builder.or(pList.toArray(new Predicate[pList.size()]));
		}
		else if (o instanceof AndPredicate) {
			AndPredicate p = (AndPredicate) o;
			List<Predicate> pList = new ArrayList<Predicate>();
			for (Object object : p.getOperandList()) {
				pList.add(predicateFromObject(builder, root, path, object));
			}
			return builder.and(pList.toArray(new Predicate[pList.size()]));
		}
		else if (o instanceof String) {
			return builder.like(this.<String>resolvePath(root, path), "%" + o + "%");
		}
		else {
			return builder.equal(resolvePath(root, path), o);
		}
	}
	
	protected <R> Path<R> resolvePath(Root<T> root, String key) {
		
		if (! key.contains(".")) {
			return root.get(key);
		}
		else {
			String[] splits = key.split("\\.");
			Path<R> path = root.get(splits[0]);
			
			for (int i=1; i<splits.length; i++) {
				path = path.get(splits[i]);
			}
			
			return path;
		}		
	}
	
	protected Order getSortOrder(CriteriaBuilder builder, Root<T> root, String sortField, GenericSortOrder sortOrder) {
		if (sortOrder == GenericSortOrder.ASC)
			return builder.asc(resolvePath(root, sortField));
		else if (sortOrder == GenericSortOrder.DESC)
			return builder.desc(resolvePath(root, sortField));
		else
			return null;
	}
}
