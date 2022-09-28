package edu.kit.scc.webreg.drools;

import java.util.List;

import org.kie.api.builder.Message;

public class DroolsCompilerException extends Exception {

	private static final long serialVersionUID = 1L;

	private List<Message> messageList;
	
	public DroolsCompilerException() {
		super();
	}

	public DroolsCompilerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DroolsCompilerException(String message) {
		super(message);
	}

	public DroolsCompilerException(String message, List<Message> messageList) {
		super(message);
		this.messageList = messageList;
	}

	public DroolsCompilerException(Throwable cause) {
		super(cause);
	}

	public List<Message> getMessageList() {
		return messageList;
	}
}
