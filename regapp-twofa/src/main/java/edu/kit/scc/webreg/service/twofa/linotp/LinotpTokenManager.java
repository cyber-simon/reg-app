package edu.kit.scc.webreg.service.twofa.linotp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import java.util.Arrays;
import java.util.HashSet;

public class LinotpTokenManager extends AbstractTwoFaManager {

	private static Logger logger = LoggerFactory.getLogger(LinotpTokenManager.class);

	private static final Set<String> capabilities = Set.of(new String[] {
			"TOTP", "YUBIKEY", "HOTP_TANLIST"
	});
	
	@Override
	public Set<String> getCapabilities() {
		if (getConfigMap().containsKey("capabilities")) {
			Set<String> capab = new HashSet<String>(Arrays.asList(getConfigMap().get("capabilities")
					.toUpperCase().split("\\s*;\\s*")));
			if (capab.retainAll(capabilities)) {
				// capab was changed -> unsupported input detected!
				logger.warn("Some provided capabilities are not supported and will be ignored!");
			}
			return capab;
		}
		return capabilities;
	}

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
			resultList.setReallyReadOnly(false);
			
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
			response.setSeed(linotpResponse.getDetail().getOtpkey().getValue());
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
	
	@Override
	public TokenStatusResponse enableToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse linotpResponse = linotpConnection.enableToken(serial);
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
	
	@Override
	public TotpCreateResponse createYubicoToken(IdentityEntity identity, String yubi, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse linotpResponse = linotpConnection.createYubicoToken(yubi);
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (linotpResponse == null || linotpResponse.getDetail() == null) {
			response.setSuccess(false);
		}
		else {
			response.setSuccess(true);
			response.setSerial(linotpResponse.getDetail().getSerial());
		}
		
		return response;
	}

	@Override
	public TotpCreateResponse createHotpBackupTanList(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse linotpResponse = linotpConnection.createBackupTanList();
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (linotpResponse == null) {
			response.setSuccess(false);
		}
		else {
			response.setSuccess(true);
			response.setSerial(linotpResponse.getDetail().getSerial());
		}
		
		return response;
	}

	@Override
	public TotpCreateResponse createPaperTanList(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		throw new IllegalAccessError();
	}
	
	@Override
	public TokenStatusResponse resetFailcounter(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse linotpResponse = linotpConnection.resetFailcounter(serial);
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
	
	@Override
	public TokenStatusResponse deleteToken(IdentityEntity identity, String serial, TokenAuditor auditor) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse linotpResponse = linotpConnection.deleteToken(serial);
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
	
	@Override
	public HmacTokenList getBackupTanList(IdentityEntity identity, String serial) throws TwoFaException {
		LinotpConnection linotpConnection = new LinotpConnection(getConfigMap());
		linotpConnection.requestAdminSession();
		
		int count = 5;
		if (getConfigMap().containsKey("backup_count")) {
			count = Integer.parseInt(getConfigMap().get("backup_count"));
		}
		LinotpGetBackupTanListResponse linotpResponse = linotpConnection.getBackupTanList(serial, count);
		
		if (linotpResponse == null) {
			throw new TwoFaException("Could not get backup tan list!");
		}
		
		HmacTokenList list = new HmacTokenList();
		list.setSerial(linotpResponse.getResult().getValue().getSerial());
		list.setTokenType(linotpResponse.getResult().getValue().getType());
		list.setOtp(new HashMap<String, String>(linotpResponse.getResult().getValue().getOtp()));
		
		return list;
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
			//LinOTP can repeatedly generate HMAC token
			((HmacToken) token).setCanGenerateOtps(true);
		}
		else {
			logger.warn("Unknown Tokentype {}. Ingoring.", linotpToken.getTokenType());
			return null;
		}
		
		token.setId(linotpToken.getId());
		token.setSerial(linotpToken.getSerial());
		token.setTokenType(linotpToken.getTokenType());
		token.setTokenInfo(linotpToken.getTokenInfo());
		token.setDescription(linotpToken.getTokenDesc());
		token.setMaxFail(linotpToken.getMaxFail());
		token.setCount(linotpToken.getCount());
		token.setUsername(linotpToken.getUsername());
		token.setSyncWindow(linotpToken.getSyncWindow());
		token.setFailCount(linotpToken.getFailCount());
		token.setIsactive(linotpToken.getIsactive());
		
		return token;
	}	
}
