package edu.kit.scc.webreg.service.twofa;

import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSetFieldResult;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;

public interface TwoFaService {

	LinotpTokenResultList findByUserId(Long userId) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(Long userId, String executor) throws TwoFaException;

	LinotpSimpleResponse enableToken(Long userId, String serial, String executor) throws TwoFaException;

	LinotpSimpleResponse checkToken(Long userId, String token) throws TwoFaException;

	Boolean hasActiveToken(Long userId) throws TwoFaException;

	LinotpSimpleResponse deleteToken(Long userId, String serial, String executor) throws TwoFaException;

	LinotpSimpleResponse checkSpecificToken(Long userId, String serial, String token) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createYubicoToken(Long userId, String yubi, String executor) throws TwoFaException;

	LinotpSetFieldResult initToken(Long userId, String serial, String executor) throws TwoFaException;

	LinotpSimpleResponse disableToken(Long userId, String serial, String executor) throws TwoFaException;

}
