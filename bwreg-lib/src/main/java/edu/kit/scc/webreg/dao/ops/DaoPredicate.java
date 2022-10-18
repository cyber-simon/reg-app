package edu.kit.scc.webreg.dao.ops;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DaoPredicate implements Serializable {

	private static final long serialVersionUID = 1L;

	protected List<Object> operandList;

	public DaoPredicate(Object... objects) {
		operandList = new ArrayList<Object>(objects.length);
		
		for (Object o : objects) {
			operandList.add(o);
		}
	}

	public List<Object> getOperandList() {
		return operandList;
	}

}