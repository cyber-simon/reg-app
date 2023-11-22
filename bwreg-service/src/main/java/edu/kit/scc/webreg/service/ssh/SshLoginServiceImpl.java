package edu.kit.scc.webreg.service.ssh;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.greaterThan;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.or;
import static edu.kit.scc.webreg.dao.ops.SortBy.descendingBy;

import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity_;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryStatus;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.SshPubKeyUsageType;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity_;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Stateless
public class SshLoginServiceImpl implements SshLoginService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserDao userDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private SshPubKeyRegistryDao sshPubKeyRegistryDao;

	@Inject
	private UserLoginInfoDao userLoginInfoDao;

	@Override
	public String authByUidNumberInteractive(ServiceEntity service, Integer uidNumber, HttpServletRequest request)
			throws RestInterfaceException {

		service = serviceDao.fetch(service.getId());

		UserEntity user = userDao.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");

		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);

		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");

		if (service.getServiceProps().containsKey("twofa")
				&& (service.getServiceProps().get("twofa").equalsIgnoreCase("enabled")
						|| service.getServiceProps().get("twofa").equalsIgnoreCase("enabled_twostep"))) {

			UserLoginInfoEntity twofaLoginInfo = findLastByRegistryAndMethod(registry.getId(), UserLoginMethod.TWOFA);
			UserLoginInfoEntity localLoginInfo = findLastByRegistryAndMethod(registry.getId(), UserLoginMethod.LOCAL);

			if (twofaLoginInfo != null && twofaLoginInfo.getLoginStatus().equals(UserLoginInfoStatus.SUCCESS)
					&& localLoginInfo != null && localLoginInfo.getLoginStatus().equals(UserLoginInfoStatus.SUCCESS)) {

				// check expiry for twofa
				Long expiry = 60L * 60L * 1000L;
				if (service.getServiceProps().containsKey("twofa_expiry")) {
					expiry = Long.parseLong(service.getServiceProps().get("twofa_expiry"));
				}

				if ((System.currentTimeMillis() - twofaLoginInfo.getLoginDate().getTime()) < expiry
						&& (System.currentTimeMillis() - localLoginInfo.getLoginDate().getTime()) < expiry) {
					List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForInteractiveLogin(registry.getId());
					return buildKeyList(regKeyList, user);
				} else {
					return "";
				}
			} else {
				return "";
			}
		} else {
			List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForInteractiveLogin(registry.getId());
			return buildKeyList(regKeyList, user);
		}
	}

	private UserLoginInfoEntity findLastByRegistryAndMethod(Long registryId, UserLoginMethod method) {
		List<UserLoginInfoEntity> list = userLoginInfoDao.findAll(withLimit(1),
				descendingBy(UserLoginInfoEntity_.loginDate),
				and(equal("registry.id", registryId), equal(UserLoginInfoEntity_.loginMethod, method)));
		return list.size() == 0 ? null : list.get(0);
	}

	private List<SshPubKeyRegistryEntity> findByRegistryForInteractiveLogin(Long registryId) {
		Date now = new Date();
		return sshPubKeyRegistryDao.findAll(and(equal("registry.id", registryId),
				equal(SshPubKeyRegistryEntity_.keyStatus, SshPubKeyRegistryStatus.ACTIVE),
				equal(SshPubKeyRegistryEntity_.usageType, SshPubKeyUsageType.INTERACTIVE),
				equal("sshPubKey.keyStatus", SshPubKeyStatus.ACTIVE),
				or(greaterThan("sshPubKey.expiresAt", now), isNull("sshPubKey.expiresAt")),
				or(greaterThan(SshPubKeyRegistryEntity_.expiresAt, now), isNull(SshPubKeyRegistryEntity_.expiresAt))));
	}

	@Override
	public String authByUidNumberCommand(ServiceEntity service, Integer uidNumber, HttpServletRequest request)
			throws RestInterfaceException {

		service = serviceDao.fetch(service.getId());

		UserEntity user = userDao.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");

		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);

		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");

		// twofa doesn't play a role with command keys
		List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForCommandLogin(registry.getId());
		return buildKeyList(regKeyList, user);
	}

	private List<SshPubKeyRegistryEntity> findByRegistryForCommandLogin(Long registryId) {
		Date now = new Date();
		return sshPubKeyRegistryDao.findAll(and(equal("registry.id", registryId),
				equal(SshPubKeyRegistryEntity_.keyStatus, SshPubKeyRegistryStatus.ACTIVE),
				equal(SshPubKeyRegistryEntity_.usageType, SshPubKeyUsageType.COMMAND),
				equal("sshPubKey.keyStatus", SshPubKeyStatus.ACTIVE),
				or(greaterThan("sshPubKey.expiresAt", now), isNull("sshPubKey.expiresAt")),
				or(greaterThan(SshPubKeyRegistryEntity_.expiresAt, now), isNull(SshPubKeyRegistryEntity_.expiresAt))));
	}

	@Override
	public String authByUidNumber(ServiceEntity service, Integer uidNumber, HttpServletRequest request)
			throws RestInterfaceException {

		service = serviceDao.fetch(service.getId());

		UserEntity user = userDao.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");

		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);

		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");

		if (service.getServiceProps().containsKey("twofa")
				&& (service.getServiceProps().get("twofa").equalsIgnoreCase("enabled")
						|| service.getServiceProps().get("twofa").equalsIgnoreCase("enabled_twostep"))) {

			UserLoginInfoEntity twofaLoginInfo = findLastByRegistryAndMethod(registry.getId(), UserLoginMethod.TWOFA);
			UserLoginInfoEntity localLoginInfo = findLastByRegistryAndMethod(registry.getId(), UserLoginMethod.LOCAL);

			if (twofaLoginInfo != null && twofaLoginInfo.getLoginStatus().equals(UserLoginInfoStatus.SUCCESS)
					&& localLoginInfo != null && localLoginInfo.getLoginStatus().equals(UserLoginInfoStatus.SUCCESS)) {

				// check expiry for twofa
				Long expiry = 60L * 60L * 1000L;
				if (service.getServiceProps().containsKey("twofa_expiry")) {
					expiry = Long.parseLong(service.getServiceProps().get("twofa_expiry"));
				}

				if ((System.currentTimeMillis() - twofaLoginInfo.getLoginDate().getTime()) < expiry
						&& (System.currentTimeMillis() - localLoginInfo.getLoginDate().getTime()) < expiry) {
					List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForLogin(registry.getId());
					logger.debug("Sending out {} keys (command and interactive, 2fa success) for registry {}",
							regKeyList.size(), registry.getId());
					return buildKeyList(regKeyList, user);
				} else {
					// always return command keys
					List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForCommandLogin(registry.getId());
					logger.debug("Sending out {} keys (only command, 2fa expired) for registry {}", regKeyList.size(),
							registry.getId());
					return buildKeyList(regKeyList, user);
				}
			} else {
				// always return command keys
				List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForCommandLogin(registry.getId());
				logger.debug("Sending out {} keys (only command, no 2fa login) for registry {}", regKeyList.size(),
						registry.getId());
				return buildKeyList(regKeyList, user);
			}
		} else {
			// return all keys if twofa is disabled for service
			List<SshPubKeyRegistryEntity> regKeyList = findByRegistryForLogin(registry.getId());
			logger.debug("Sending out {} keys (command and interactive, 2fa disabled for service) for registry {}",
					regKeyList.size(), registry.getId());
			return buildKeyList(regKeyList, user);
		}
	}

	private List<SshPubKeyRegistryEntity> findByRegistryForLogin(Long registryId) {
		Date now = new Date();
		return sshPubKeyRegistryDao.findAll(and(equal("registry.id", registryId),
				equal(SshPubKeyRegistryEntity_.keyStatus, SshPubKeyRegistryStatus.ACTIVE),
				equal("sshPubKey.keyStatus", SshPubKeyStatus.ACTIVE),
				or(greaterThan("sshPubKey.expiresAt", now), isNull("sshPubKey.expiresAt")),
				or(greaterThan(SshPubKeyRegistryEntity_.expiresAt, now), isNull(SshPubKeyRegistryEntity_.expiresAt))));
	}

	protected String buildKeyList(List<SshPubKeyRegistryEntity> regKeyList, UserEntity user) {
		StringBuffer sb = new StringBuffer();
		for (SshPubKeyRegistryEntity regKey : regKeyList) {
			if (regKey.getUsageType().equals(SshPubKeyUsageType.COMMAND)) {
				sb.append("command=\"");
				sb.append(regKey.getCommand());
				sb.append("\",from=\"");
				sb.append(regKey.getFrom());
				sb.append("\" ");
			}
			sb.append(regKey.getSshPubKey().getKeyType());
			sb.append(" ");
			sb.append(regKey.getSshPubKey().getEncodedKey().replaceAll("[\\n\t ]", ""));
			if (user.getEmail() != null) {
				sb.append(" ");
				sb.append(user.getEmail());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
