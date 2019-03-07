package edu.kit.scc.webreg.ssh;

public class UnsupportedKeyTypeException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnsupportedKeyTypeException() {
		super();
	}

	public UnsupportedKeyTypeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UnsupportedKeyTypeException(String arg0) {
		super(arg0);
	}

	public UnsupportedKeyTypeException(Throwable arg0) {
		super(arg0);
	}
}
