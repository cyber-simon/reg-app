package edu.kit.scc.webreg.service.twofa.linotp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.TokenAuditor;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.AbstractTwoFaManager;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.token.GenericTwoFaToken;
import edu.kit.scc.webreg.service.twofa.token.HmacToken;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpCreateResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpToken;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;
import edu.kit.scc.webreg.service.twofa.token.YubicoToken;

public class LinotpTokenManager extends AbstractTwoFaManager {

	private static Logger logger = LoggerFactory.getLogger(LinotpTokenManager.class);

	@Override
	public TwoFaTokenList findByIdentity(IdentityEntity identity) throws TwoFaException {
		TwoFaTokenList resultList = new TwoFaTokenList();
		if (getConfigMap().containsKey("reallyReadOnly") && getConfigMap().get("reallyReadOnly").equalsIgnoreCase("true")) {
			resultList.setReallyReadOnly(true);
			resultList.setReadOnly(true);
			if (getConfigMap().containsKey("managementUrl")) {
				resultList.setManagementUrl(getConfigMap().get("managementUrl"));
			}
		}
		else {
			LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
			linotpConnection.requestAdminSession();

			LinotpShowUserResponse response = linotpConnection.getTokenList();
			if (response.getResult() != null && response.getResult().getValue() != null &&
					response.getResult().getValue().getData() !=null) {
				
				for (LinotpToken linotpToken : response.getResult().getValue().getData()) {
					GenericTwoFaToken token = convertToken(linotpToken);

					if (token != null) {
						resultList.add(token);
					}
				}
			}
			
			if (getConfigMap().containsKey("readOnly") && getConfigMap().get("readOnly").equalsIgnoreCase("true")) {
				resultList.setReadOnly(true);
			}
			else {
				resultList.setReadOnly(false);
			}
	
			if (getConfigMap().containsKey("managementUrl")) {
				resultList.setManagementUrl(getConfigMap().get("managementUrl"));
			}
	
			if (getConfigMap().containsKey("adminRole")) {
				resultList.setAdminRole(getConfigMap().get("adminRole"));
			}
	
		}
		return resultList;
	}
	
	@Override
	public Boolean hasActiveToken(IdentityEntity identity) throws TwoFaException {
		TwoFaTokenList tokenList = findByIdentity(identity);

		if (tokenList.getReallyReadOnly()) {
			return true;
		}
		
		for (GenericTwoFaToken token : tokenList) {
			if (token.getIsactive()) {
				/*
				 * filter token, that are not initialized
				 */
				if (token.getDescription() != null && token.getDescription().contains("INIT")) {
					return false;
				}
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Boolean checkToken(IdentityEntity identity, String token) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		LinotpSimpleResponse response = linotpConnection.checkToken(token);
		
		if (!(response.getResult() != null && response.getResult().isStatus() && 
				response.getResult().isValue())) {
			return false;
		}
		else {
			return true;
		}
	}
	
	@Override
	public Boolean checkSpecificToken(IdentityEntity identity, String serial, String token) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		LinotpSimpleResponse response = linotpConnection.checkSpecificToken(serial, token);
		
		if (!(response.getResult() != null && response.getResult().isStatus() && 
				response.getResult().isValue())) {
			return false;
		}
		else {
			return true;
		}	
	}
	
	@Override
	public Map<String,Object> initToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		LinotpSetFieldResult response = linotpConnection.initToken(serial);
		return response.getValue();
	}
	
	@Override
	public TotpCreateResponse createAuthenticatorToken(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse linotpResponse = linotpConnection.createAuthenticatorToken();
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (linotpResponse.getResult().isStatus() && linotpResponse.getResult().isValue()) {
			response.setSuccess(true);
			response.setSerial(linotpResponse.getDetail().getSerial());
			response.setDescription(linotpResponse.getDetail().getGoogleurl().getDescription());
			response.setImage(linotpResponse.getDetail().getGoogleurl().getImg());
			response.setOrder(linotpResponse.getDetail().getGoogleurl().getOrder());
			response.setValue(linotpResponse.getDetail().getGoogleurl().getValue());
		}
		else {
			response.setSuccess(false);
		}
		
		return response;
	}

	@Override
	public TokenStatusResponse disableToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse linotpResponse = linotpConnection.disableToken(serial);
		TokenStatusResponse response = new TokenStatusResponse();
		if ((linotpResponse.getResult() != null) && linotpResponse.getResult().isStatus() &&
				linotpResponse.getResult().isValue()) {
			response.setSuccess(true);
		}
		else {
			response.setSuccess(false);
		}
		response.setSerial(serial);
		
		return response;
	}
	
	private GenericTwoFaToken convertToken(LinotpToken linotpToken) {
		GenericTwoFaToken token;
		if (linotpToken.getTokenType().equals("TOTP")) {
			TotpToken totpToken = new TotpToken();
			totpToken.setOtpLen(linotpToken.getOtpLen());
			totpToken.setCountWindow(linotpToken.getCountWindow());
			token = totpToken;
		}
		else if (linotpToken.getTokenType().equals("yubico")) {
			token = new YubicoToken();
		}
		else if (linotpToken.getTokenType().equals("HMAC")) {
			token = new HmacToken();
		}
		else {
			logger.warn("Unknown Tokentype {}. Ingoring.", linotpToken.getTokenType());
			return null;
		}
		
		token.setId(linotpToken.getId());
		token.setSerial(linotpToken.getSerial());
		token.setTokenType(linotpToken.getTokenType());
		token.setTokenInfo(linotpToken.getTokenInfo());
		token.setDescription(linotpToken.getDescription());
		token.setMaxFail(linotpToken.getMaxFail());
		token.setCount(linotpToken.getCount());
		token.setUsername(linotpToken.getUsername());
		token.setSyncWindow(linotpToken.getSyncWindow());
		token.setFailCount(linotpToken.getFailCount());
		token.setIsactive(linotpToken.getIsactive());
		
		return token;
	}	
}
