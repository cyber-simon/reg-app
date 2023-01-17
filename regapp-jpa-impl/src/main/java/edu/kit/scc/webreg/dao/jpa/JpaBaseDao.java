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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ops.AndPredicate;
import edu.kit.scc.webreg.dao.ops.DaoFilterData;
import edu.kit.scc.webreg.dao.ops.DaoMatchMode;
import edu.kit.scc.webreg.dao.ops.DaoSortData;
import edu.kit.scc.webreg.dao.ops.MultipathOrPredicate;
import edu.kit.scc.webreg.dao.ops.NotEqualsObjectValue;
import edu.kit.scc.webreg.dao.ops.NotLikeObjectValue;
import edu.kit.scc.webreg.dao.ops.OrPredicate;
import edu.kit.scc.webreg.dao.ops.PathObjectValue;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class JpaBaseDao<T extends BaseEntity> implements BaseDao<T> {

	@PersistenceContext
	protected EntityManager em;

	public abstract Class<T> getEntityClass();

	@Override
	public T createNew() {
		try {
			return getEntityClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
				| InvocationTargetException e) {
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
	public List<T> findAllPaging(int offset, int limit, Map<String, DaoSortData> sortBy,
			Map<String, Object> filterMap, Map<String, DaoFilterData> additionalFilterMap, String... attrs) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> root = criteria.from(getEntityClass());

		List<Predicate> predicates = predicatesFromFilterMap(builder, root, filterMap);
		predicates.addAll(predicatesFromAdditionalFilterMap(builder, root, additionalFilterMap));
		criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

		criteria.select(root);
		if (attrs != null) {
			criteria.distinct(true);

			for (String attr : attrs)
				root.fetch(attr, JoinType.LEFT);
		}

		if (sortBy != null) {
			criteria.orderBy(getSortOrder(builder, root, sortBy));
		}

		Query q = em.createQuery(criteria);
		q.setFirstResult(offset).setMaxResults(limit);

		return q.getResultList();
	}

	@Override
	public Number countAll(Map<String, Object> filterMap, Map<String, DaoFilterData> additionalFilterMap) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<T> root = criteria.from(getEntityClass());

		List<Predicate> predicates = predicatesFromFilterMap(builder, root, filterMap);
		predicates.addAll(predicatesFromAdditionalFilterMap(builder, root, additionalFilterMap));

		criteria.select(builder.count(root));
		criteria.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));

		TypedQuery<Long> q = em.createQuery(criteria);
		return q.getSingleResult();
	}

	@Override
	public T findById(Long id) {
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
	public List<T> findByMultipleId(List<Long> ids) {
		if (ids.size() == 0)
			return new ArrayList<T>();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		In<Object> inClause = builder.in(entity.get("id"));
		for (Object id : ids) {
			inClause.value(id);
		}
		criteria.where(inClause);
		criteria.select(entity);

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public T findByAttr(String attr, Object value) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		criteria.where(builder.and(builder.equal(entity.get(attr), value)));
		criteria.select(entity);

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<T> findAllByAttr(String attr, Object value) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		Predicate p = predicateFromObject(builder, entity, attr, value);
		criteria.where(p);
		criteria.select(entity);

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public T findByIdWithAttrs(Long id, String... attrs) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		criteria.where(builder.and(builder.equal(entity.get("id"), id)));
		criteria.select(entity);
		criteria.distinct(true);

		for (String attr : attrs)
			entity.fetch(attr, JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	protected List<Predicate> predicatesFromFilterMap(CriteriaBuilder builder, Root<T> root,
			Map<String, Object> filterMap) {

		List<Predicate> predicates = new ArrayList<Predicate>();
		if (filterMap != null) {
			for (Entry<String, Object> entry : filterMap.entrySet()) {

				predicates.add(predicateFromObject(builder, root, entry.getKey(), entry.getValue()));
			}
		}

		return predicates;

	}

	protected List<Predicate> predicatesFromAdditionalFilterMap(CriteriaBuilder builder, Root<T> root,
			Map<String, DaoFilterData> additionalFilterMap) {

		List<Predicate> predicates = new ArrayList<Predicate>();

		if (additionalFilterMap != null) {
			for (Entry<String, DaoFilterData> entry : additionalFilterMap.entrySet()) {
				if (entry.getValue() != null && entry.getValue().getFilterValue() != null) {
					predicates.add(predicateFromFilterMeta(builder, root, entry.getKey(), entry.getValue()));
				}
			}
		}

		return predicates;
	}

	protected Predicate predicateFromFilterMeta(CriteriaBuilder builder, Root<T> root, String path,
			DaoFilterData filterMeta) {
		if (filterMeta.getMatchMode().equals(DaoMatchMode.STARTS_WITH)) {
			return builder.like(builder.lower(this.<String>resolvePath(root, path)),
					filterMeta.getFilterValue().toString().toLowerCase() + "%");
		} else if (filterMeta.getMatchMode().equals(DaoMatchMode.ENDS_WITH)) {
			return builder.like(builder.lower(this.<String>resolvePath(root, path)),
					"%" + filterMeta.getFilterValue().toString().toLowerCase());
		} else if (filterMeta.getMatchMode().equals(DaoMatchMode.CONTAINS)) {
			return builder.like(builder.lower(this.<String>resolvePath(root, path)),
					"%" + filterMeta.getFilterValue().toString().toLowerCase() + "%");
		} else if (filterMeta.getMatchMode().equals(DaoMatchMode.EQUALS)) {
			return builder.like(builder.lower(this.<String>resolvePath(root, path)),
					filterMeta.getFilterValue().toString().toLowerCase());
		} else {
			return builder.equal(resolvePath(root, path), filterMeta.getFilterValue());
		}
	}

	protected Predicate predicateFromObject(CriteriaBuilder builder, Root<T> root, String path, Object o) {
		if (o == null) {
			return builder.isNull(resolvePath(root, path));
		} else if (o instanceof MultipathOrPredicate) {
			MultipathOrPredicate p = (MultipathOrPredicate) o;
			List<Predicate> pList = new ArrayList<Predicate>();
			for (Object object : p.getOperandList()) {
				PathObjectValue pov = (PathObjectValue) object;
				pList.add(predicateFromObject(builder, root, pov.getPath(), pov.getValue()));
			}
			return builder.or(pList.toArray(new Predicate[pList.size()]));
		} else if (o instanceof OrPredicate) {
			OrPredicate p = (OrPredicate) o;
			List<Predicate> pList = new ArrayList<Predicate>();
			for (Object object : p.getOperandList()) {
				pList.add(predicateFromObject(builder, root, path, object));
			}
			return builder.or(pList.toArray(new Predicate[pList.size()]));
		} else if (o instanceof AndPredicate) {
			AndPredicate p = (AndPredicate) o;
			List<Predicate> pList = new ArrayList<Predicate>();
			for (Object object : p.getOperandList()) {
				pList.add(predicateFromObject(builder, root, path, object));
			}
			return builder.and(pList.toArray(new Predicate[pList.size()]));
		} else if (o instanceof NotEqualsObjectValue) {
			NotEqualsObjectValue p = (NotEqualsObjectValue) o;
			return builder.notEqual(resolvePath(root, p.getPath()), p.getValue());
		} else if (o instanceof NotLikeObjectValue) {
			NotLikeObjectValue p = (NotLikeObjectValue) o;
			String s = (String) p.getValue();
			return builder.notLike(builder.lower(this.<String>resolvePath(root, p.getPath())),
					"%" + s.toLowerCase() + "%");
		} else if (o instanceof String) {
			String s = (String) o;
			return builder.like(builder.lower(this.<String>resolvePath(root, path)), "%" + s.toLowerCase() + "%");
		} else {
			return builder.equal(resolvePath(root, path), o);
		}
	}

	@SuppressWarnings("unchecked")
	protected <R> Path<R> resolvePath(Root<T> root, String key) {
		Path<R> path = (Path<R>) root;
		for (String fieldName : key.split("\\.")) {
			path = path.get(fieldName);
		}
		return path;
	}

	protected List<Order> getSortOrder(CriteriaBuilder builder, Root<T> root, Map<String, DaoSortData> sortBy) {
		List<Order> orderList = new ArrayList<Order>();
		if (sortBy != null) {
			for (Entry<String, DaoSortData> entry : sortBy.entrySet()) {
				getSortOrder(builder, root, entry.getValue()).ifPresent(orderList::add);
			}
		}
		return orderList;
	}

	protected Optional<Order> getSortOrder(CriteriaBuilder builder, Root<T> root, DaoSortData sortOrder) {
		switch (sortOrder.getOrder()) {
		case ASCENDING:
			return Optional.of(builder.asc(resolvePath(root, sortOrder.getField())));
		case DESCENDING:
			return Optional.of(builder.desc(resolvePath(root, sortOrder.getField())));
		default:
			return Optional.empty();
		}
	}

}
