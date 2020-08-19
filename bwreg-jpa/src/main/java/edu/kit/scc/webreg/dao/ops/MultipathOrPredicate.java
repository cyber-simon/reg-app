package edu.kit.scc.webreg.dao.ops;


public class MultipathOrPredicate extends DaoPredicate {

	private static final long serialVersionUID = 1L;

	public MultipathOrPredicate(PathObjectValue... objects) {
		super((Object []) objects);
	}
}
