package edu.kit.scc.webreg.exc;


public class NoHostnameConfiguredException extends RestInterfaceException {

	private static final long serialVersionUID = 1L;

	public NoHostnameConfiguredException() {
		super();
	}

	public NoHostnameConfiguredException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoHostnameConfiguredException(String message) {
		super(message);
	}

	public NoHostnameConfiguredException(Throwable cause) {
		super(cause);
	}

}
