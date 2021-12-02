package edu.kit.scc.webreg.service.twofa;

import java.util.Map;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;

public interface TwoFaManager {

	void setConfigMap(Map<String, String> configMap);
	TwoFaTokenList findByIdentity(IdentityEntity identity) throws TwoFaException;
	Boolean hasActiveToken(IdentityEntity identity) throws TwoFaException;
}
