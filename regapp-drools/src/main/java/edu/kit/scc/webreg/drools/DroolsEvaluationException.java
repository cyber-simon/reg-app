package edu.kit.scc.webreg.drools;

import java.util.List;

import org.kie.api.builder.Message;

public class DroolsEvaluationException extends Exception {

	private static final long serialVersionUID = 1L;

	public DroolsEvaluationException() {
		super();
	}

	public DroolsEvaluationException(String message, Throwable cause) {
		super(message, cause);
	}

	public DroolsEvaluationException(String message) {
		super(message);
	}

	public DroolsEvaluationException(Throwable cause) {
		super(cause);
	}
}
