package edu.kit.scc.webreg.dao.ops;

import java.util.List;

public class Or implements RqlExpression {

	private final List<RqlExpression> operands;

	protected Or(List<RqlExpression> operands) {
		this.operands = operands;
	}

	public List<RqlExpression> getOperands() {
		return operands;
	}

}
