package edu.kit.scc.webreg.service.twofa.token;

public class HmacToken extends GenericTwoFaToken {

	private static final long serialVersionUID = 1L;

	private Boolean canGenerateOtps = false;

	public Boolean getCanGenerateOtps() {
		return canGenerateOtps;
	}

	public void setCanGenerateOtps(Boolean canGenerateOtps) {
		this.canGenerateOtps = canGenerateOtps;
	}
}
