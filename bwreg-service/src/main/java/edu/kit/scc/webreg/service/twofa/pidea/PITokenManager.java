package edu.kit.scc.webreg.service.twofa.pidea;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.TokenAuditor;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.AbstractTwoFaManager;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.token.GenericTwoFaToken;
import edu.kit.scc.webreg.service.twofa.token.HmacToken;
import edu.kit.scc.webreg.service.twofa.token.HmacTokenList;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpCreateResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpToken;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;
import edu.kit.scc.webreg.service.twofa.token.YubicoToken;

public class PITokenManager extends AbstractTwoFaManager {

	private static Logger logger = LoggerFactory.getLogger(PITokenManager.class);

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
			PIConnection PIConnection = new PIConnection(getConfigMap());
			PIConnection.requestAdminSession();

			PIShowUserResponse response = PIConnection.getTokenList();
			if (response.getResult() != null && response.getResult().getValue() != null &&
					response.getResult().getValue().getData() !=null) {
				
				for (PIToken piToken : response.getResult().getValue().getData()) {
					GenericTwoFaToken token = convertToken(piToken);

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

		if (tokenList.getReallyReadOnly() != null && tokenList.getReallyReadOnly()) {
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
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PISimpleResponse response = PIConnection.checkToken(token);
		
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
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PISimpleResponse response = PIConnection.checkSpecificToken(serial, token);
		
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
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		PISetFieldResult response = PIConnection.initToken(serial);
		return response.getValue();
	}
	
	@Override
	public TotpCreateResponse createAuthenticatorToken(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		
		PIInitAuthenticatorTokenResponse piResponse = PIConnection.createAuthenticatorToken();
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (piResponse.getResult().isStatus() && piResponse.getResult().isValue()) {
			response.setSuccess(true);
			response.setSerial(piResponse.getDetail().getSerial());
			response.setDescription(piResponse.getDetail().getGoogleurl().getDescription());
			response.setImage(piResponse.getDetail().getGoogleurl().getImg());
			response.setOrder(piResponse.getDetail().getGoogleurl().getOrder());
			response.setValue(piResponse.getDetail().getGoogleurl().getValue());
			response.setSeed(piResponse.getDetail().getOtpkey().getValue());
		}
		else {
			response.setSuccess(false);
		}
		
		return response;
	}

	@Override
	public TokenStatusResponse disableToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		PISimpleResponse piResponse = PIConnection.disableToken(serial);
		TokenStatusResponse response = new TokenStatusResponse();
		if ((piResponse.getResult() != null) && piResponse.getResult().isStatus() &&
				piResponse.getResult().isValue()) {
			response.setSuccess(true);
		}
		else {
			response.setSuccess(false);
		}
		response.setSerial(serial);
		
		return response;
	}
	
	@Override
	public TokenStatusResponse enableToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		PISimpleResponse piResponse = PIConnection.enableToken(serial);
		TokenStatusResponse response = new TokenStatusResponse();
		if ((piResponse.getResult() != null) && piResponse.getResult().isStatus() &&
				piResponse.getResult().isValue()) {
			response.setSuccess(true);
		}
		else {
			response.setSuccess(false);
		}
		response.setSerial(serial);
		
		return response;
	}
	
	@Override
	public TotpCreateResponse createYubicoToken(IdentityEntity identity, String yubi, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		
		PIInitAuthenticatorTokenResponse piResponse = PIConnection.createYubicoToken(yubi);
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (piResponse == null || piResponse.getDetail() == null) {
			response.setSuccess(false);
		}
		else {
			response.setSuccess(true);
			response.setSerial(piResponse.getDetail().getSerial());
		}
		
		return response;
	}

	@Override
	public TotpCreateResponse createBackupTanList(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		
		PIInitAuthenticatorTokenResponse piResponse = PIConnection.createBackupTanList();
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (piResponse == null) {
			response.setSuccess(false);
		}
		else {
			response.setSuccess(true);
			response.setSerial(piResponse.getDetail().getSerial());
		}
		
		return response;
	}

	@Override
	public TokenStatusResponse resetFailcounter(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		PISimpleResponse piResponse = PIConnection.resetFailcounter(serial);
		TokenStatusResponse response = new TokenStatusResponse();

		if ((piResponse.getResult() != null) && piResponse.getResult().isStatus() &&
				piResponse.getResult().isValue()) {
			response.setSuccess(true);
		}
		else {
			response.setSuccess(false);
		}
		response.setSerial(serial);
		
		return response;
	}
	
	@Override
	public TokenStatusResponse deleteToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		PISimpleResponse piResponse = PIConnection.deleteToken(serial);
		TokenStatusResponse response = new TokenStatusResponse();

		if ((piResponse.getResult() != null) && piResponse.getResult().isStatus() &&
				piResponse.getResult().isValue()) {
			response.setSuccess(true);
		}
		else {
			response.setSuccess(false);
		}
		response.setSerial(serial);
		
		return response;
	}
	
	@Override
	public HmacTokenList getBackupTanList(IdentityEntity identity, String serial) throws TwoFaException {
		PIConnection PIConnection = new PIConnection(getConfigMap());
		PIConnection.requestAdminSession();
		
		int count = 5;
		if (getConfigMap().containsKey("backup_count")) {
			count = Integer.parseInt(getConfigMap().get("backup_count"));
		}
		PIGetBackupTanListResponse piResponse = PIConnection.getBackupTanList(serial, count);
		
		if (piResponse == null) {
			throw new TwoFaException("Could not get backup tan list!");
		}
		
		HmacTokenList list = new HmacTokenList();
		list.setSerial(piResponse.getResult().getValue().getSerial());
		list.setTokenType(piResponse.getResult().getValue().getType());
		list.setOtp(new HashMap<String, String>(piResponse.getResult().getValue().getOtp()));
		
		return list;
	}
	
	private GenericTwoFaToken convertToken(PIToken piToken) {
		GenericTwoFaToken token;
		if (piToken.getTokenType().equals("TOTP")) {
			TotpToken totpToken = new TotpToken();
			totpToken.setOtpLen(piToken.getOtpLen());
			totpToken.setCountWindow(piToken.getCountWindow());
			token = totpToken;
		}
		else if (piToken.getTokenType().equals("yubico")) {
			token = new YubicoToken();
		}
		else if (piToken.getTokenType().equals("HMAC")) {
			token = new HmacToken();
		}
		else {
			logger.warn("Unknown Tokentype {}. Ingoring.", piToken.getTokenType());
			return null;
		}
		
		token.setId(piToken.getId());
		token.setSerial(piToken.getSerial());
		token.setTokenType(piToken.getTokenType());
		token.setTokenInfo(piToken.getTokenInfo());
		token.setDescription(piToken.getTokenDesc());
		token.setMaxFail(piToken.getMaxFail());
		token.setCount(piToken.getCount());
		token.setUsername(piToken.getUsername());
		token.setSyncWindow(piToken.getSyncWindow());
		token.setFailCount(piToken.getFailCount());
		token.setIsactive(piToken.getIsactive());
		
		return token;
	}	
}
