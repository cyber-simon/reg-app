package edu.kit.scc.webreg.dao.ops;

import java.util.List;

public class And implements RqlExpression {

	private final List<RqlExpression> operands;

	protected And(List<RqlExpression> operands) {
		this.operands = operands;
	}

	public List<RqlExpression> getOperands() {
		return operands;
	}

}
