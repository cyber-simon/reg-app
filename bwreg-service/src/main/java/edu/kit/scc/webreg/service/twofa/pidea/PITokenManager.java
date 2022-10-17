package edu.kit.scc.webreg.service.twofa.pidea;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.TokenAuditor;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.twofa.AbstractTwoFaManager;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.token.GenericTwoFaToken;
import edu.kit.scc.webreg.service.twofa.token.HmacToken;
import edu.kit.scc.webreg.service.twofa.token.HmacTokenList;
import edu.kit.scc.webreg.service.twofa.token.PaperTanListToken;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpCreateResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpToken;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;
import edu.kit.scc.webreg.service.twofa.token.YubicoToken;
import java.util.Arrays;
import java.util.HashSet;

public class PITokenManager extends AbstractTwoFaManager {

	private static Logger logger = LoggerFactory.getLogger(PITokenManager.class);

	private static final Set<String> capabilities = Set.of(new String[] {
			"TOTP", "YUBIKEY", "PAPER_TAN"
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
			
			PIConnection PIConnection = new PIConnection(getConfigMap());
			PIConnection.requestAdminSession();

			PIShowUserResponse response = PIConnection.getTokenList();
			if (response.getResult() != null && response.getResult().getValue() != null &&
					response.getResult().getValue().getTokens() !=null) {
				
				for (PIToken piToken : response.getResult().getValue().getTokens()) {
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
			response.setImage("<img src=\"" + piResponse.getDetail().getGoogleurl().getImg() + "\" width=\"128\" height=\"128\" />");
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
	public TotpCreateResponse createHotpBackupTanList(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		throw new IllegalAccessError();
	}

	@Override
	public TotpCreateResponse createPaperTanList(IdentityEntity identity, TokenAuditor auditor) throws TwoFaException {
		PIConnection piConnection = new PIConnection(getConfigMap());
		piConnection.requestAdminSession();
		
		PIInitPaperTanTokenResponse piResponse = piConnection.createPaperTanList();
		TotpCreateResponse response = new TotpCreateResponse();
		
		if (piResponse == null || piResponse.getDetail() == null) {
			response.setSuccess(false);
		}
		else {
			response.setSuccess(true);
			response.setSerial(piResponse.getDetail().getSerial());
			if (getConfigMap().containsKey("papertan_count")) {
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				AtomicInteger i = new AtomicInteger(0);
				int max = Integer.parseInt(getConfigMap().get("papertan_count"));
				
				piResponse.getDetail().getOtps().forEach( (x, y) -> {
					if (i.incrementAndGet() <= max) { 
						map.put(x, y);
					}
				});
				response.setOtps(map);
			}
			else {
				response.setOtps(piResponse.getDetail().getOtps());
			}
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
		throw new IllegalAccessError();
	}
	
	private GenericTwoFaToken convertToken(PIToken piToken) {
		GenericTwoFaToken token;
		if (piToken.getTokentype().equals("totp")) {
			TotpToken totpToken = new TotpToken();
			totpToken.setOtpLen(piToken.getOtplen());
			totpToken.setCountWindow(piToken.getCountWindow());
			token = totpToken;
			token.setTokenType("TOTP");
		}
		else if (piToken.getTokentype().equals("yubico")) {
			token = new YubicoToken();
			token.setTokenType("yubico");
		}
		else if (piToken.getTokentype().equals("hotp")) {
			token = new HmacToken();
			token.setTokenType("HMAC");
		}
		else if (piToken.getTokentype().equals("paper")) {
			PaperTanListToken paperToken = new PaperTanListToken();
			paperToken.setTokenType("PAPER_TAN");
			token = paperToken;
		}
		else {
			logger.warn("Unknown Tokentype {}. Ingoring.", piToken.getTokentype());
			return null;
		}
		
		// Token in PI only have serials
		//token.setId(piToken.getId());
		token.setSerial(piToken.getSerial());
		//token.setTokenInfo(piToken.getTokenInfo());
		token.setDescription(piToken.getDescription());
		token.setMaxFail(piToken.getMaxfail());
		token.setCount(piToken.getCount());
		token.setUsername(piToken.getUsername());
		//token.setSyncWindow(piToken.getSyncWindow());
		token.setFailCount(piToken.getFailcount());
		token.setIsactive(piToken.getActive());
		
		return token;
	}	
}
