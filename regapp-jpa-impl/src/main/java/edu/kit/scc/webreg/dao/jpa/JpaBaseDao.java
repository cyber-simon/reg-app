/*******************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html Contributors: Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.NullOrder.NULL_FIRST;
import static edu.kit.scc.webreg.dao.ops.NullOrder.NULL_LAST;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.in;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;
import static edu.kit.scc.webreg.dao.ops.SortOrder.ASCENDING;
import static edu.kit.scc.webreg.dao.ops.SortOrder.DESCENDING;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ops.And;
import edu.kit.scc.webreg.dao.ops.Equal;
import edu.kit.scc.webreg.dao.ops.EqualIgnoreCase;
import edu.kit.scc.webreg.dao.ops.GreaterThan;
import edu.kit.scc.webreg.dao.ops.In;
import edu.kit.scc.webreg.dao.ops.IsNotNull;
import edu.kit.scc.webreg.dao.ops.IsNull;
import edu.kit.scc.webreg.dao.ops.LessThan;
import edu.kit.scc.webreg.dao.ops.LessThanOrEqualTo;
import edu.kit.scc.webreg.dao.ops.Like;
import edu.kit.scc.webreg.dao.ops.LikeMatchMode;
import edu.kit.scc.webreg.dao.ops.NotEqual;
import edu.kit.scc.webreg.dao.ops.NotIn;
import edu.kit.scc.webreg.dao.ops.NotLike;
import edu.kit.scc.webreg.dao.ops.NullOrder;
import edu.kit.scc.webreg.dao.ops.Or;
import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.dao.ops.SortByBasedOnAttribute;
import edu.kit.scc.webreg.dao.ops.SortByBasedOnString;
import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.AbstractBaseEntity_;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class JpaBaseDao<T extends BaseEntity> implements BaseDao<T> {

	@Inject
	protected EntityManager em;

	public abstract Class<T> getEntityClass();

	@Override
	public T createNew() {
		try {
			return getEntityClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
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
		if (em.contains(entity)) {
			return entity;
		} else {
			return em.merge(entity);
		}
	}

	@Override
	public void refresh(T entity) {
		em.refresh(entity);
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
	public List<T> findAll() {
		return findAll(null, ascendingBy(AbstractBaseEntity_.id), null);
	}

	@Override
	public List<T> findAll(PaginateBy paginateBy) {
		return findAll(paginateBy, ascendingBy(AbstractBaseEntity_.id), null);
	}

	@Override
	public List<T> findAll(RqlExpression filterBy) {
		return findAll(null, List.of(ascendingBy(AbstractBaseEntity_.id)), filterBy);
	}

	@Override
	public List<T> findAll(PaginateBy paginateBy, RqlExpression filterBy) {
		return findAll(paginateBy, ascendingBy(AbstractBaseEntity_.id), filterBy);
	}

	@Override
	public List<T> findAll(PaginateBy paginateBy, SortBy sortBy, RqlExpression filterBy) {
		return findAll(paginateBy, List.of(sortBy), filterBy);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public List<T> findAll(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> root = criteria.from(getEntityClass());
		criteria.select(root);

		if (filterBy != null) {
			applyFilter(criteria, builder, root, filterBy);
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

	private <Q> void applyFilter(CriteriaQuery<Q> criteria, CriteriaBuilder builder, Root<T> root, RqlExpression filterBy) {
		criteria.where(mapRqlExpressionToPredicate(builder, root, filterBy));
	}

	@SuppressWarnings("unchecked")
	private Predicate mapRqlExpressionToPredicate(CriteriaBuilder builder, Root<T> root, RqlExpression rqlExpression) {
		if (rqlExpression instanceof And) {
			Predicate[] predicates = ((And) rqlExpression).getOperands()
					.stream()
					.map(e -> mapRqlExpressionToPredicate(builder, root, e))
					.toArray(Predicate[]::new);
			return builder.and(predicates);
		} else if (rqlExpression instanceof Or) {
			Predicate[] predicates = ((Or) rqlExpression).getOperands()
					.stream()
					.map(e -> mapRqlExpressionToPredicate(builder, root, e))
					.toArray(Predicate[]::new);
			return builder.or(predicates);
		} else if (rqlExpression instanceof IsNull) {
			IsNull<T, ?> isNull = (IsNull<T, ?>) rqlExpression;
			return builder.isNull(isNull.getFieldPath(root));
		} else if (rqlExpression instanceof IsNotNull) {
			IsNotNull<T, ?> isNotNull = (IsNotNull<T, ?>) rqlExpression;
			return builder.isNotNull(isNotNull.getFieldPath(root));
		} else if (rqlExpression instanceof GreaterThan) {
			@SuppressWarnings("rawtypes")
			GreaterThan greaterThan = (GreaterThan) rqlExpression;
			return builder.greaterThan(greaterThan.getFieldPath(root), greaterThan.getAssignedValue());
		} else if (rqlExpression instanceof In) {
			In<T, ?> in = (In<T, ?>) rqlExpression;
			return in.getFieldPath(root).in(in.getAssignedValues());
		} else if (rqlExpression instanceof NotIn) {
			NotIn<T, ?> notIn = (NotIn<T, ?>) rqlExpression;
			return notIn.getFieldPath(root).in(notIn.getAssignedValues()).not();
		} else if (rqlExpression instanceof LessThan) {
			@SuppressWarnings("rawtypes")
			LessThan lessThan = (LessThan) rqlExpression;
			return builder.lessThan(lessThan.getFieldPath(root), lessThan.getAssignedValue());
		} else if (rqlExpression instanceof LessThanOrEqualTo) {
			@SuppressWarnings("rawtypes")
			LessThanOrEqualTo lessThanOrEqualTo = (LessThanOrEqualTo) rqlExpression;
			return builder.lessThanOrEqualTo(lessThanOrEqualTo.getFieldPath(root), lessThanOrEqualTo.getAssignedValue());
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
		} else if (rqlExpression instanceof EqualIgnoreCase) {
			EqualIgnoreCase<T> equal = (EqualIgnoreCase<T>) rqlExpression;
			return builder.equal(builder.lower(equal.getFieldPath(root)), equal.getAssignedValue().toLowerCase());
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void applyJoinFetch(CriteriaQuery<T> criteria, Root<T> root, Attribute... joinFetchBy) {
		criteria.distinct(true);
		for (Attribute field : joinFetchBy) {
			if (field instanceof SingularAttribute) {
				root.fetch((SingularAttribute) field, JoinType.LEFT);
			} else {
				root.fetch((PluralAttribute) field, JoinType.LEFT);
			}
		}
	}

	private void applySorting(CriteriaQuery<T> criteria, CriteriaBuilder builder, Root<T> root, List<SortBy> sortBy) {
		List<Order> orders = sortBy.stream()
				.map(by -> getOrder(builder, root, by))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(Order.class::cast)
				.collect(Collectors.toList());
		criteria.orderBy(orders);
	}

	private Optional<Order> getOrder(CriteriaBuilder builder, Root<T> root, SortBy sortBy) {
		switch (sortBy.getSortOrder()) {
			case ASCENDING: {
				Expression<T> nullAdjustedField = getNullAdjustedFieldExpression(builder, root, sortBy);
				return Optional.of(builder.asc(nullAdjustedField));
			}
			case DESCENDING: {
				Expression<T> nullAdjustedField = getNullAdjustedFieldExpression(builder, root, sortBy);
				return Optional.of(builder.desc(nullAdjustedField));
			}
			default:
				return Optional.empty();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Expression<T> getNullAdjustedFieldExpression(CriteriaBuilder builder, Root<T> root, SortBy sortBy) {
		if (sortBy.getNullOrder() != null && !NullOrder.DB_DEFAULT.equals(sortBy.getNullOrder())) {
			if (sortBy instanceof SortByBasedOnString) {
				throw new UnsupportedOperationException(
						String.format("NULL FIRST or NULL LAST is only supported for %s", SortByBasedOnAttribute.class.getSimpleName()));
			}
			return (Expression) builder.coalesce(sortBy.getFieldPath(root), getNullReplacementForField(sortBy));
		} else {
			return sortBy.getFieldPath(root);
		}
	}

	@SuppressWarnings("unchecked")
	private Object getNullReplacementForField(SortBy sortBy) {
		Class<T> javaTypeOfField = (Class<T>) ((SortByBasedOnAttribute) sortBy).getField().getJavaType();
		boolean replaceWithMaxValue = ASCENDING.equals(sortBy.getSortOrder()) && NULL_LAST.equals(sortBy.getNullOrder())
				|| DESCENDING.equals(sortBy.getSortOrder()) && NULL_FIRST.equals(sortBy.getNullOrder());
		Object replacement = null;
		if (Integer.class.equals(javaTypeOfField)) {
			replacement = replaceWithMaxValue ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		} else if (Long.class.equals(javaTypeOfField)) {
			replacement = replaceWithMaxValue ? Long.MAX_VALUE : Long.MIN_VALUE;
		} else if (Date.class.equals(javaTypeOfField)) {
			replacement = replaceWithMaxValue ? new Date(9224318015999999L) // 294276-12-31 23:59:59.999
					: new Date(-210866803200000L); // -4713-01-01 00:00:00
		} else if (String.class.equals(javaTypeOfField)) {
			if (replaceWithMaxValue) {
				throw new UnsupportedOperationException(String.format("Combination of %s with %s is not supported for String typed fields",
						sortBy.getSortOrder(), sortBy.getNullOrder()));
			}
			replacement = "";
		} else {
			throw new UnsupportedOperationException(
					String.format("NULL FIRST or NULL LAST is not supported for type %s", javaTypeOfField.getSimpleName()));
		}
		return replacement;
	}

	protected void applyPaging(Query query, PaginateBy paginateBy) {
		query.setFirstResult(paginateBy.getOffset());
		if (paginateBy.getLimit() > 0) {
			query.setMaxResults(paginateBy.getLimit());
		}
	}

	@Override
	public List<T> fetchAll(List<Long> ids) {
		return ids.size() == 0 ? new ArrayList<>() : findAllEagerly(in(AbstractBaseEntity_.id, ids));
	}

	@Override
	public Number countAll(RqlExpression filterBy) {
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
	@SuppressWarnings("rawtypes")
	public T find(RqlExpression findBy, Attribute... joinFetchBy) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> entity = criteria.from(getEntityClass());
		criteria.select(entity);
		criteria.where(mapRqlExpressionToPredicate(builder, entity, findBy));

		if (joinFetchBy != null) {
			applyJoinFetch(criteria, entity, joinFetchBy);
		}

		try {
			return em.createQuery(criteria).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public T fetch(Long id) {
		return em.find(getEntityClass(), id);
	}

	@Override
	public T fetch(Long id, LockModeType lockMode) {
		return em.find(getEntityClass(), id, lockMode);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<T> findAllEagerly(RqlExpression filterBy, Attribute... joinFetchBy) {
		return findAllEagerly(null, List.of(ascendingBy(AbstractBaseEntity_.id)), filterBy, joinFetchBy);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> findAllEagerly(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy, Attribute... joinFetchBy) {
		List<Long> ids = findIds(paginateBy, sortBy, filterBy);
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> root = criteria.from(getEntityClass());
		criteria.select(root);
		applyFilter(criteria, builder, root, in(AbstractBaseEntity_.id, ids));

		if (joinFetchBy != null) {
			applyJoinFetch(criteria, root, joinFetchBy);
		}
		if (sortBy != null) {
			applySorting(criteria, builder, root, sortBy);
		}

		Query query = em.createQuery(criteria);

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	private List<Long> findIds(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(getEntityClass());
		Root<T> root = criteria.from(getEntityClass());
		criteria.select(root);

		if (filterBy != null) {
			applyFilter(criteria, builder, root, filterBy);
		}
		if (sortBy != null) {
			applySorting(criteria, builder, root, sortBy);
		}

		Query query = em.createQuery(criteria);
		if (paginateBy != null) {
			applyPaging(query, paginateBy);
		}
		return (List<Long>) query.getResultStream().map(e -> ((AbstractBaseEntity) e).getId()).collect(Collectors.toList());
	}

}
