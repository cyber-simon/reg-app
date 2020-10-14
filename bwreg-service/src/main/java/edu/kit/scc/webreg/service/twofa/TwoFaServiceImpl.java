package edu.kit.scc.webreg.service.twofa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.TokenAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
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
	private IdentityDao identityDao;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
	@Inject
	private AuditEntryDao auditEntryDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private ApplicationConfig appConfig;

	@Override
	public LinotpTokenResultList findByIdentity(IdentityEntity identity) throws TwoFaException {
		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpShowUserResponse response = linotpConnection.getTokenList();
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
	public Boolean hasActiveToken(IdentityEntity identity) throws TwoFaException {
		List<LinotpToken> tokenList = findByIdentity(identity);
		
		for (LinotpToken token : tokenList) {
			if (token.getIsactive()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Boolean hasActiveTokenById(Long identityId) throws TwoFaException {
		IdentityEntity identity = identityDao.findById(identityId);
		List<LinotpToken> tokenList = findByIdentity(identity);
		
		for (LinotpToken token : tokenList) {
			if (token.getIsactive()) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public LinotpSimpleResponse checkToken(IdentityEntity identity, String token) throws TwoFaException {
		Map<String, String> configMap = configResolver.resolveConfig(identity);
		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		return linotpConnection.checkToken(token);
	}

	@Override
	public LinotpSimpleResponse checkSpecificToken(IdentityEntity identity, String serial, String token) throws TwoFaException {
		Map<String, String> configMap = configResolver.resolveConfig(identity);
		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		return linotpConnection.checkSpecificToken(serial, token);
	}

	@Override
	public LinotpSetFieldResult initToken(IdentityEntity identity, String serial, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);
		
		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-InitToken-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Init token " + serial + " for identity " + identity.getId());
		
		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSetFieldResult response = linotpConnection.initToken(serial);
	
		auditor.logAction("" + identity.getId(), "INIT TOTP TOKEN", "serial-" + serial, "", AuditStatus.SUCCESS);
		
		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("identity", identity);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_INIT, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		auditor.finishAuditTrail();
		
		return response;
	}
	
	@Override
	public LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(IdentityEntity identity, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);

		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-CreateAuthenticatorToken-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Creating authenticator token for user " + identity.getId());

		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createAuthenticatorToken();
		
		if (response.getResult().isStatus() && response.getResult().isValue()) {
			// Token successfully created

			auditor.logAction("" + identity.getId(), "CREATE TOTP TOKEN", "serial-" + response.getDetail().getSerial(), "", AuditStatus.SUCCESS);
			
			HashMap<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("identity", identity);
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
			
			auditor.logAction("" + identity.getId(), "DISABLE TOTP TOKEN", "serial-" + response.getDetail().getSerial(), "", AuditStatus.SUCCESS);
			auditor.finishAuditTrail();

			return response;
		}
		else {
			auditor.logAction("" + identity, "CREATE TOTP TOKEN", "", "", AuditStatus.FAIL);
			auditor.finishAuditTrail();
			throw new TwoFaException("Token generation did not succeed!");
		}
	}

	@Override
	public LinotpInitAuthenticatorTokenResponse createYubicoToken(IdentityEntity identity, String yubi, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);

		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-CreateYubicoToken-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Creating yubico token for user " + identity.getId());

		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createYubicoToken(yubi);
		
		if (response == null || response.getDetail() == null) {
			auditor.logAction("" + identity.getId(), "CREATE YUBICO TOKEN", "", "", AuditStatus.FAIL);
			auditor.finishAuditTrail();
			throw new TwoFaException("Token generation did not succeed!");
		}

		auditor.logAction("" + identity.getId(), "CREATE YUBICO TOKEN", "serial-" + response.getDetail().getSerial(), "", AuditStatus.SUCCESS);

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("identity", identity);
		eventMap.put("respone", response);
		if (response.getDetail() != null)
			eventMap.put("serial", response.getDetail().getSerial());
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_CREATED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		auditor.finishAuditTrail();
		return response;
	}

	@Override
	public LinotpInitAuthenticatorTokenResponse createBackupTanList(IdentityEntity identity, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);

		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-CreateBackupTanList-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Creating backup tan list for user " + identity.getId());

		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		
		LinotpInitAuthenticatorTokenResponse response = linotpConnection.createBackupTanList();
		
		if (response == null) {
			auditor.logAction("" + identity.getId(), "CREATE BACKUP TAN LIST", "", "", AuditStatus.FAIL);
			auditor.finishAuditTrail();
			throw new TwoFaException("Token generation did not succeed!");
		}

		auditor.logAction("" + identity.getId(), "CREATE BACKUP TAN LIST", "serial-" + response.getDetail().getSerial(), "", AuditStatus.SUCCESS);

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("identity", identity);
		eventMap.put("respone", response);
		if (response.getDetail() != null)
			eventMap.put("serial", response.getDetail().getSerial());
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_CREATED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		auditor.finishAuditTrail();

		return response;
	}

	@Override
	public LinotpGetBackupTanListResponse getBackupTanList(IdentityEntity identity, String serial, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);
		
		Map<String, String> configMap = configResolver.resolveConfig(identity);

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
	public LinotpSimpleResponse disableToken(IdentityEntity identity, String serial, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);

		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-DisableToken-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Disable token " + serial + " for user " + identity.getId());

		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse response = linotpConnection.disableToken(serial);

		auditor.logAction("" + identity.getId(), "DISABLE TOKEN", "serial-" + serial, "", AuditStatus.SUCCESS);
		
		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("identity", identity);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_DISABLED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		auditor.finishAuditTrail();
		
		return response;
	}
	
	@Override
	public LinotpSimpleResponse enableToken(IdentityEntity identity, String serial, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);

		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-EnableToken-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Enable token " + serial + " for user " + identity.getId());

		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse response = linotpConnection.enableToken(serial);

		auditor.logAction("" + identity.getId(), "ENABLE TOKEN", "serial-" + serial, "", AuditStatus.SUCCESS);

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("identity", identity);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_ENABLED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		auditor.finishAuditTrail();
		
		return response;
	}
	
	@Override
	public LinotpSimpleResponse deleteToken(IdentityEntity identity, String serial, String executor) throws TwoFaException {
		identity = identityDao.merge(identity);

		TokenAuditor auditor = new TokenAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName(this.getClass().getName() + "-DeleteToken-Audit");
		auditor.setIdentity(identity);
		auditor.setDetail("Delete token " + serial + " for user " + identity.getId());
		
		Map<String, String> configMap = configResolver.resolveConfig(identity);

		LinotpConnection linotpConnection = new LinotpConnection(configMap);
		linotpConnection.requestAdminSession();
		LinotpSimpleResponse response = linotpConnection.deleteToken(serial);

		auditor.logAction("" + identity.getId(), "DELETE TOKEN", "serial-" + serial, "", AuditStatus.SUCCESS);

		HashMap<String, Object> eventMap = new HashMap<String, Object>();
		eventMap.put("identity", identity);
		eventMap.put("respone", response);
		eventMap.put("serial", serial);
		TokenEvent event = new TokenEvent(eventMap);
		try {
			eventSubmitter.submit(event, EventType.TWOFA_DELETED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		auditor.finishAuditTrail();

		return response;
	}
	
}
