package edu.kit.scc.webreg.service.twofa;

import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;

public interface TwoFaService {

	LinotpTokenResultList findByUserId(Long userId) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(Long userId) throws TwoFaException;

	LinotpSimpleResponse disableToken(Long userId, String serial) throws TwoFaException;

	LinotpSimpleResponse enableToken(Long userId, String serial) throws TwoFaException;

}
