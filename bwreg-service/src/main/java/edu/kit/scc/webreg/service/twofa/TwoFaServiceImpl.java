package edu.kit.scc.webreg.service.twofa;

import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpConnection;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpShowUserResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;

@Stateless
public class TwoFaServiceImpl implements TwoFaService {

	@Inject
	private Logger logger;
	
	@Inject
	private TwoFaConfigurationResolver configResolver;
	
	@Inject
	private UserDao userDao;
	
	@Override
	public LinotpTokenResultList findByUserId(Long userId) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpShowUserResponse response = linotpConnection.getTokenList(user);
		LinotpTokenResultList resultList = new LinotpTokenResultList();
		if (response.getResult() != null && response.getResult().getValue() != null &&
				response.getResult().getValue().getData() !=null) {
			resultList.addAll(response.getResult().getValue().getData());
		}
		
		if (configMap.containsKey("readOnly") && configMap.get("readOnly").equalsIgnoreCase("true")) {
			resultList.setReadOnly(true);
		}
		else {
			resultList.setReadOnly(false);
		}

		if (configMap.containsKey("managementUrl")) {
			resultList.setManagementUrl(configMap.get("managementUrl"));
		}

		return resultList;
	}
	
	@Override
	public LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(Long userId) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createAuthenticatorToken(user);
		
		if (response.getResult().isStatus() && response.getResult().isValue()) {
			// Token succeful created
			// Disable it for once
			linotpConnection.disableToken(response.getDetail().getSerial());
			return response;
		}
		else {
			throw new TwoFaException("Token generation did not succeed!");
		}
	}
	
	@Override
	public LinotpSimpleResponse disableToken(Long userId, String serial) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		return linotpConnection.disableToken(serial);
	}
	
	@Override
	public LinotpSimpleResponse enableToken(Long userId, String serial) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		return linotpConnection.enableToken(serial);
	}
}
