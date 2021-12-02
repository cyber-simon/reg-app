package edu.kit.scc.webreg.service.twofa;

import java.util.Map;

import edu.kit.scc.webreg.audit.TokenAuditor;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpCreateResponse;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;

public interface TwoFaManager {

	void setConfigMap(Map<String, String> configMap);
	TwoFaTokenList findByIdentity(IdentityEntity identity) throws TwoFaException;
	Boolean hasActiveToken(IdentityEntity identity) throws TwoFaException;
	Boolean checkToken(IdentityEntity identity, String token) throws TwoFaException;
	Boolean checkSpecificToken(IdentityEntity identity, String serial, String token) throws TwoFaException;
	Map<String,Object> initToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException;
	TotpCreateResponse createAuthenticatorToken(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException;
	TokenStatusResponse disableToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException;
	TotpCreateResponse createYubicoToken(IdentityEntity identity, String yubi, TokenAuditor auditor) throws TwoFaException;
	TotpCreateResponse createBackupTanList(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException;
}
