package edu.kit.scc.webreg.dao.ops;

import java.util.List;

public interface NotIn<E, F> extends Predicate<E, F> {

	List<F> getAssignedValues();

}
