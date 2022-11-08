package edu.kit.scc.webreg.service.saml.exc;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public class NoIdpFoundException extends RestInterfaceException {

	private static final long serialVersionUID = 1L;

	public NoIdpFoundException() {
		super();
	}

	public NoIdpFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoIdpFoundException(String message) {
		super(message);
	}

	public NoIdpFoundException(Throwable cause) {
		super(cause);
	}

}
