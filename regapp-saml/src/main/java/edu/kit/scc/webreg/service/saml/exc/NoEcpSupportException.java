package edu.kit.scc.webreg.service.saml.exc;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public class NoEcpSupportException extends RestInterfaceException {

	private static final long serialVersionUID = 1L;

	public NoEcpSupportException() {
		super();
	}

	public NoEcpSupportException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoEcpSupportException(String message) {
		super(message);
	}

	public NoEcpSupportException(Throwable cause) {
		super(cause);
	}

}
