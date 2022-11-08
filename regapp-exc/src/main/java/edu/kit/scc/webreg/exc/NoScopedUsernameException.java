package edu.kit.scc.webreg.exc;


public class NoScopedUsernameException extends RestInterfaceException {

	private static final long serialVersionUID = 1L;

	public NoScopedUsernameException() {
		super();
	}

	public NoScopedUsernameException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoScopedUsernameException(String message) {
		super(message);
	}

	public NoScopedUsernameException(Throwable cause) {
		super(cause);
	}

}
