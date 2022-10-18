package edu.kit.scc.webreg.dao.ops;

public class NotLikeObjectValue {

	private String path;
	private Object value;
	
	public NotLikeObjectValue(String path, Object value) {
		super();
		this.path = path;
		this.value = value;
	}

	public String getPath() {
		return path;
	}
	
	public Object getValue() {
		return value;
	}
}
