package edu.kit.scc.webreg.dao.ops;

public class DaoFilterData {

	DaoMatchMode matchMode;
	Object filterValue;

	public DaoMatchMode getMatchMode() {
		return matchMode;
	}

	public void setMatchMode(DaoMatchMode matchMode) {
		this.matchMode = matchMode;
	}

	public Object getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(Object filterValue) {
		this.filterValue = filterValue;
	}
	
}
