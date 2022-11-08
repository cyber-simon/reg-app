package edu.kit.scc.webreg.service.twofa;

public class TwoFaException extends Exception {

	private static final long serialVersionUID = 1L;

	public TwoFaException() {
		super();
	}

	public TwoFaException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public TwoFaException(String arg0) {
		super(arg0);
	}

	public TwoFaException(Throwable arg0) {
		super(arg0);
	}
}
