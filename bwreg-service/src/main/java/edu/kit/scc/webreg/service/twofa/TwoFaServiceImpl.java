package edu.kit.scc.webreg.service.twofa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.TokenEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpConnection;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpGetBackupTanListResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSetFieldResult;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpShowUserResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpToken;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;

@Stateless
public class TwoFaServiceImpl implements TwoFaService {

	@Inject
	private Logger logger;
	
	@Inject
	private TwoFaConfigurationResolver configResolver;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
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

		if (configMap.containsKey("adminRole")) {
			resultList.setAdminRole(configMap.get("adminRole"));
		}

		return resultList;
	}

	@Override
	public Boolean hasActiveToken(Long userId) throws TwoFaException {
		List<LinotpToken> tokenList = findByUserId(userId);
		
		for (LinotpToken token : tokenList) {
			if (token.getIsactive()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public LinotpSimpleResponse checkToken(Long userId, String token) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		return linotpConnection.checkToken(user, token);
	}

	@Override
	public LinotpSimpleResponse checkSpecificToken(Long userId, String serial, String token) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		return linotpConnection.checkSpecificToken(serial, token);
	}

	@Override
	public LinotpSetFieldResult initToken(Long userId, String serial, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSetFieldResult response = linotpConnection.initToken(serial);
		
		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("user", user);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_INIT, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return response;
	}
	
	@Override
	public LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(Long userId, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createAuthenticatorToken(user);
		
		if (response.getResult().isStatus() && response.getResult().isValue()) {
			// Token succeful created
			
			HashMap<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("user", user);
			eventMap.put("respone", response);
			if (response.getDetail() != null)
				eventMap.put("serial", response.getDetail().getSerial());
			TokenEvent event = new TokenEvent(eventMap);
			try {
				eventSubmitter.submit(event, EventType.TWOFA_CREATED, executor);
			} catch (EventSubmitException e) {
				logger.warn("Could not submit event", e);
			}
			
			// Disable it for once
			linotpConnection.disableToken(response.getDetail().getSerial());
			return response;
		}
		else {
			throw new TwoFaException("Token generation did not succeed!");
		}
	}

	@Override
	public LinotpInitAuthenticatorTokenResponse createYubicoToken(Long userId, String yubi, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createYubicoToken(user, yubi);
		
		if (response == null) {
			throw new TwoFaException("Token generation did not succeed!");
		}

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("user", user);
		eventMap.put("respone", response);
		if (response.getDetail() != null)
			eventMap.put("serial", response.getDetail().getSerial());
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_CREATED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return response;
	}

	@Override
	public LinotpInitAuthenticatorTokenResponse createBackupTanList(Long userId, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createBackupTanList(user);
		
		if (response == null) {
			throw new TwoFaException("Token generation did not succeed!");
		}

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("user", user);
		eventMap.put("respone", response);
		if (response.getDetail() != null)
			eventMap.put("serial", response.getDetail().getSerial());
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_CREATED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return response;
	}

	@Override
	public LinotpGetBackupTanListResponse getBackupTanList(Long userId, String serial, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		int count = 5;
		if (configMap.containsKey("backup_count")) {
			count = Integer.parseInt(configMap.get("backup_count"));
		}
		LinotpGetBackupTanListResponse response = linotpConnection.getBackupTanList(serial, count);
		
		if (response == null) {
			throw new TwoFaException("Could not get backup tan list!");
		}

		return response;
	}

	@Override
	public LinotpSimpleResponse disableToken(Long userId, String serial, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse response = linotpConnection.disableToken(serial);
		
		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("user", user);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_DISABLED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		
		return response;
	}
	
	@Override
	public LinotpSimpleResponse enableToken(Long userId, String serial, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse response = linotpConnection.enableToken(serial);
		
		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("user", user);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_ENABLED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return response;
	}
	
	@Override
	public LinotpSimpleResponse deleteToken(Long userId, String serial, String executor) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse response = linotpConnection.deleteToken(serial);

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("user", user);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_DELETED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return response;
	}
	
}
