package edu.kit.scc.webreg.service.twofa;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.token.HmacTokenList;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpCreateResponse;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;

public interface TwoFaService {

	TwoFaTokenList findByIdentity(IdentityEntity identity) throws TwoFaException;

	TotpCreateResponse createAuthenticatorToken(IdentityEntity identity, String executor) throws TwoFaException;

	TokenStatusResponse enableToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	Boolean checkToken(IdentityEntity identity, String token) throws TwoFaException;

	Boolean hasActiveToken(IdentityEntity identity) throws TwoFaException;

	TokenStatusResponse deleteToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	Boolean checkSpecificToken(IdentityEntity identity, String serial, String token) throws TwoFaException;

	TotpCreateResponse createYubicoToken(IdentityEntity identity, String yubi, String executor) throws TwoFaException;

	void initToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	TokenStatusResponse disableToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	TotpCreateResponse createBackupTanList(IdentityEntity identity, String executor) throws TwoFaException;

	HmacTokenList getBackupTanList(IdentityEntity identity, String serial)
			throws TwoFaException;

	Boolean hasActiveTokenById(Long identityId) throws TwoFaException;

	TokenStatusResponse resetFailcounter(IdentityEntity identity, String serial, String executor)
			throws TwoFaException;

}
