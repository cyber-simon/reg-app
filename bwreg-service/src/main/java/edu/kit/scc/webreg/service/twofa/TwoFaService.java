package edu.kit.scc.webreg.service.twofa;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpGetBackupTanListResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSetFieldResult;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;

public interface TwoFaService {

	LinotpTokenResultList findByIdentity(IdentityEntity identity) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(IdentityEntity identity, String executor) throws TwoFaException;

	LinotpSimpleResponse enableToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	LinotpSimpleResponse checkToken(IdentityEntity identity, String token) throws TwoFaException;

	Boolean hasActiveToken(IdentityEntity identity) throws TwoFaException;

	LinotpSimpleResponse deleteToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	LinotpSimpleResponse checkSpecificToken(IdentityEntity identity, String serial, String token) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createYubicoToken(IdentityEntity identity, String yubi, String executor) throws TwoFaException;

	LinotpSetFieldResult initToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	LinotpSimpleResponse disableToken(IdentityEntity identity, String serial, String executor) throws TwoFaException;

	LinotpInitAuthenticatorTokenResponse createBackupTanList(IdentityEntity identity, String executor) throws TwoFaException;

	LinotpGetBackupTanListResponse getBackupTanList(IdentityEntity identity, String serial, String executor)
			throws TwoFaException;

	Boolean hasActiveTokenById(Long identityId) throws TwoFaException;

}
