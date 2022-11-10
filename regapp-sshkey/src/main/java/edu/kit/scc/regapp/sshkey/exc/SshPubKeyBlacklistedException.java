package edu.kit.scc.regapp.sshkey.exc;

public class SshPubKeyBlacklistedException extends Exception {

	private static final long serialVersionUID = 1L;

	public SshPubKeyBlacklistedException() {
		super();
	}

	public SshPubKeyBlacklistedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SshPubKeyBlacklistedException(String arg0) {
		super(arg0);
	}

	public SshPubKeyBlacklistedException(Throwable arg0) {
		super(arg0);
	}
}
