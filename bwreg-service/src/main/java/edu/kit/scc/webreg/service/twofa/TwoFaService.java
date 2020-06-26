package edu.kit.scc.webreg.service.twofa;

import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;

public interface TwoFaService {

	LinotpTokenResultList findByUserId(Long userId) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(Long userId) throws TwoFaException;

}
