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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.in;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ops.And;
import edu.kit.scc.webreg.dao.ops.Equal;
import edu.kit.scc.webreg.dao.ops.In;
import edu.kit.scc.webreg.dao.ops.IsNull;
import edu.kit.scc.webreg.dao.ops.Like;
import edu.kit.scc.webreg.dao.ops.LikeMatchMode;
import edu.kit.scc.webreg.dao.ops.NotEqual;
import edu.kit.scc.webreg.dao.ops.NotLike;
import edu.kit.scc.webreg.dao.ops.Or;
import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.dao.ops.SortOrder;
import edu.kit.scc.webreg.entity.AbstractBaseEntity_;
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

	@Override
	public List<T> findAll() {
		return findAllPaging(null, SortBy.of(AbstractBaseEntity_.id, SortOrder.ASCENDING), null);
	}

	@Override
	public List<T> findAllPaging(RqlExpression filterBy) {
		return findAllPaging(null, SortBy.of(AbstractBaseEntity_.id, SortOrder.ASCENDING), filterBy);
	}

	@Override
	public List<T> findAllPaging(PaginateBy paginateBy) {
		return findAllPaging(paginateBy, SortBy.of(AbstractBaseEntity_.id, SortOrder.ASCENDING), null);
	}

	@Override
	public List<T> findAllPaging(PaginateBy paginateBy, RqlExpression filterBy) {
		return findAllPaging(paginateBy, SortBy.of(AbstractBaseEntity_.id, SortOrder.ASCENDING), filterBy);
	}

	@Override
	public List<T> findAllPaging(PaginateBy paginateBy, SortBy sortBy, RqlExpression filterBy) {
		return findAllPaging(paginateBy, List.of(sortBy), filterBy);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final List<T> findAllPaging(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy,
			String... joinFetchBy) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> root = criteria.from(getEntityClass());
		criteria.select(root);

		if (filterBy != null) {
			applyFilter(criteria, builder, root, filterBy);
		}
		if (joinFetchBy != null) {
			applyJoinFetch(criteria, root, joinFetchBy);
		}
		if (sortBy != null) {
			applySorting(criteria, builder, root, sortBy);
		}

		Query query = em.createQuery(criteria);
		if (paginateBy != null) {
			applyPaging(query, paginateBy);
		}
		return query.getResultList();
	}

	private <Q> void applyFilter(CriteriaQuery<Q> criteria, CriteriaBuilder builder, Root<T> root,
			RqlExpression filterBy) {
		criteria.where(mapRqlExpressionToPredicate(builder, root, filterBy));
	}

	@SuppressWarnings("unchecked")
	private Predicate mapRqlExpressionToPredicate(CriteriaBuilder builder, Root<T> root, RqlExpression rqlExpression) {
		if (rqlExpression instanceof And) {
			Predicate[] predicates = ((And) rqlExpression).getOperands().stream()
					.map(e -> mapRqlExpressionToPredicate(builder, root, e)).toArray(Predicate[]::new);
			return builder.and(predicates);
		} else if (rqlExpression instanceof Or) {
			Predicate[] predicates = (Predicate[]) ((Or) rqlExpression).getOperands().stream()
					.map(e -> mapRqlExpressionToPredicate(builder, root, e)).toArray(Predicate[]::new);
			return builder.or(predicates);
		} else if (rqlExpression instanceof IsNull) {
			IsNull<T, ?> isNull = (IsNull<T, ?>) rqlExpression;
			return builder.isNull(isNull.getFieldPath(root));
		} else if (rqlExpression instanceof In) {
			In<T, ?> in = (In<T, ?>) rqlExpression;
			javax.persistence.criteria.CriteriaBuilder.In<Object> inPredicate = builder.in(in.getFieldPath(root));
			in.getAssignedValues().forEach(inPredicate::value);
			return inPredicate;
		} else if (rqlExpression instanceof Like) {
			Like<T> like = (Like<T>) rqlExpression;
			return builder.like(builder.lower(like.getFieldPath(root)),
					applyWildcardsToPattern(like.getPattern().toLowerCase(), like.getMatchMode()));
		} else if (rqlExpression instanceof NotLike) {
			NotLike<T> notLike = (NotLike<T>) rqlExpression;
			return builder.notLike(builder.lower(notLike.getFieldPath(root)),
					applyWildcardsToPattern(notLike.getPattern().toLowerCase(), notLike.getMatchMode()));
		} else if (rqlExpression instanceof Equal) {
			Equal<T, ?> equal = (Equal<T, ?>) rqlExpression;
			return builder.equal(equal.getFieldPath(root), equal.getAssignedValue());
		} else if (rqlExpression instanceof NotEqual) {
			NotEqual<T, ?> notEqual = (NotEqual<T, ?>) rqlExpression;
			return builder.notEqual(notEqual.getFieldPath(root), notEqual.getAssignedValue());
		} else {
			throw new UnsupportedOperationException(
					String.format("Expression '%s' is not supported", rqlExpression.getClass().getSimpleName()));
		}
	}

	private String applyWildcardsToPattern(String pattern, LikeMatchMode matchMode) {
		switch (matchMode) {
		case STARTS_WITH:
			return pattern + "%";
		case ENDS_WITH:
			return "%" + pattern;
		case CONTAINS:
			return "%" + pattern + "%";
		case EQUALS:
			return pattern;
		default:
			throw new UnsupportedOperationException(String.format("Match mode '%s' is not supported", matchMode));
		}
	}

	@SafeVarargs
	private void applyJoinFetch(CriteriaQuery<T> criteria, Root<T> root, String... joinFetchBy) {
		criteria.distinct(true);
		for (String field : joinFetchBy) {
			root.fetch(field, JoinType.LEFT);
		}
	}

	private void applySorting(CriteriaQuery<T> criteria, CriteriaBuilder builder, Root<T> root, List<SortBy> sortBy) {
		List<Order> orders = sortBy.stream().map(by -> getOrder(builder, root, by)).filter(Optional::isPresent)
				.map(Optional::get).map(Order.class::cast).collect(Collectors.toList());
		criteria.orderBy(orders);
	}

	protected void applyPaging(Query query, PaginateBy paginateBy) {
		query.setFirstResult(paginateBy.getOffset());
		if (paginateBy.getLimit() > 0) {
			query.setMaxResults(paginateBy.getLimit());
		}
	}

	@Override
	public final Number countAll(RqlExpression filterBy) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<T> root = criteria.from(getEntityClass());
		criteria.select(builder.count(root));
		if (filterBy != null) {
			applyFilter(criteria, builder, root, filterBy);
		}
		return em.createQuery(criteria).getSingleResult();
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
		return ids.size() == 0 ? new ArrayList<T>() : findAllPaging(in("id", ids));
	}

	@Override
	public T findByAttr(String attr, Object value) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		criteria.select(entity);
		criteria.where(builder.equal(entity.get(attr), value));

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public T findByIdWithAttrs(Long id, String... attrs) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		criteria.select(entity);
		criteria.where(builder.equal(entity.get("id"), id));

		if (attrs != null) {
			applyJoinFetch(criteria, entity, attrs);
		}

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	private Optional<Order> getOrder(CriteriaBuilder builder, Root<T> root, SortBy sortOrder) {
		switch (sortOrder.getOrder()) {
		case ASCENDING:
			return Optional.of(builder.asc(sortOrder.getFieldPath(root)));
		case DESCENDING:
			return Optional.of(builder.desc(sortOrder.getFieldPath(root)));
		default:
			return Optional.empty();
		}
	}

}
