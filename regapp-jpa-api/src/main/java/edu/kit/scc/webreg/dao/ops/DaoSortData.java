package edu.kit.scc.webreg.dao.ops;

public class DaoSortData {

	private DaoSortOrder order;
	private String field;

	public DaoSortData() {
		super();
	}

	public DaoSortData(String field, DaoSortOrder order) {
		this.order = order;
		this.field = field;
	}

	public DaoSortOrder getOrder() {
		return order;
	}

	public void setOrder(DaoSortOrder order) {
		this.order = order;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
