package edu.kit.scc.webreg.exc;


public class NoIdpForScopeException extends RestInterfaceException {

	private static final long serialVersionUID = 1L;

	public NoIdpForScopeException() {
		super();
	}

	public NoIdpForScopeException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoIdpForScopeException(String message) {
		super(message);
	}

	public NoIdpForScopeException(Throwable cause) {
		super(cause);
	}

}
