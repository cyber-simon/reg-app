package edu.kit.scc.webreg.drools;

public class DroolsConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DroolsConfigurationException() {
		super();
	}

	public DroolsConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DroolsConfigurationException(String message) {
		super(message);
	}

	public DroolsConfigurationException(Throwable cause) {
		super(cause);
	}
}
