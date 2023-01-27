package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class NotLikeBasedOnAttribute<E> implements NotLike<E> {

	private final SingularAttribute<E, String> field;
	private final String pattern;
	private final LikeMatchMode matchMode;

	protected NotLikeBasedOnAttribute(SingularAttribute<E, String> field, String pattern, LikeMatchMode matchMode) {
		this.field = field;
		this.pattern = pattern;
		this.matchMode = matchMode;
	}

	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public LikeMatchMode getMatchMode() {
		return matchMode;
	}

	@Override
	public Path<String> getFieldPath(Path<E> parent) {
		return parent.get(field);
	}

}
