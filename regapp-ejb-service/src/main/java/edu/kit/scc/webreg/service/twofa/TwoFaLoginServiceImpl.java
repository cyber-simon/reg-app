package edu.kit.scc.webreg.service.twofa;

import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Stateless
public class TwoFaLoginServiceImpl implements TwoFaLoginService {

	@Inject
	private Logger logger;

	@Inject
	private TwoFaService twoFaService;

	@Inject
	private UserDao userDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private UserLoginInfoDao userLoginInfoDao;

	@Override
	public String otpLogin(String eppn, String serviceShortName, String otp, String secret, HttpServletRequest request,
			Boolean checkRegistry) throws TwoFaException, RestInterfaceException {

		logger.debug("New otpLogin for {} and {}", eppn, serviceShortName);

		ServiceEntity service = serviceDao.findByShortName(serviceShortName);
		if (service == null)
			throw new NoServiceFoundException("no such service");

		RegistryEntity registry = null;
		UserEntity user = null;

		if (eppn != null) {
			if (eppn.contains("@")) {
				List<UserEntity> userList = userDao.findByEppn(eppn);
				if (userList.size() == 0)
					throw new NoUserFoundException("no such user");
				else if (userList.size() > 1)
					throw new NoUserFoundException("user eppn not unique");
				user = userList.get(0);

				registry = findRegistry(user, service);
				if (checkRegistry && registry == null)
					throw new NoRegistryFoundException("user not registered for service");
			} else {
				/*
				 * eppn is not an eppn, but a localUid
				 */
				List<RegistryEntity> registryList = registryDao.findAllByRegValueAndStatus(service, "localUid", eppn,
						RegistryStatus.ACTIVE);
				if (registryList.size() == 0) {
					registryList.addAll(registryDao.findAllByRegValueAndStatus(service, "localUid", eppn,
							RegistryStatus.LOST_ACCESS));
				}
				if (registryList.size() == 0) {
					registryList.addAll(
							registryDao.findAllByRegValueAndStatus(service, "localUid", eppn, RegistryStatus.ON_HOLD));
				}
				if (registryList.size() == 0) {
					throw new NoUserFoundException("no such localUid in registries");
				}
				registry = registryList.get(0);
				user = registry.getUser();
			}
		} else {
			// username is not set
			return ":-(";
		}

		if (!service.getServiceProps().containsKey("twofa_validate_secret")) {
			logger.warn(
					"No validation secret configured for service {}. Please configure service property twofa_validate_secret",
					service.getShortName());
			return ":-(";
		} else if ((secret == null) || (!secret.equals(service.getServiceProps().get("twofa_validate_secret")))) {
			logger.warn("validation secret mismatch for service {}, {}", service.getShortName(),
					request.getRemoteAddr());
			return ":-(";
		}

		if (otp == null || otp.equals("")) {
			createLoginInfo(user, registry, UserLoginMethod.TWOFA, UserLoginInfoStatus.FAILED);
			throw new LoginFailedException("Password blank");
		}

		Boolean success = twoFaService.checkToken(user.getIdentity(), otp);

		if (!success) {
			logger.info("User {} ({}) failed 2fa authentication", user.getEppn(), user.getId());
			createLoginInfo(user, registry, UserLoginMethod.TWOFA, UserLoginInfoStatus.FAILED);
			return ":-(";
		} else {
			logger.info("User {} ({}) 2fa authentication success", user.getEppn(), user.getId());
			createLoginInfo(user, registry, UserLoginMethod.TWOFA, UserLoginInfoStatus.SUCCESS);
			return ":-)";
		}
	}

	private RegistryEntity findRegistry(UserEntity user, ServiceEntity service) {
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);

		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be
			 * rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
		}

		if (registry == null) {
			/*
			 * Also check for On_hold registries. They should also be allowed to be
			 * rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ON_HOLD);
		}

		return registry;
	}

	private void createLoginInfo(UserEntity user, RegistryEntity registry, UserLoginMethod method,
			UserLoginInfoStatus status) {
		UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
		loginInfo.setUser(user);
		loginInfo.setRegistry(registry);
		loginInfo.setLoginDate(new Date());
		loginInfo.setLoginMethod(method);
		loginInfo.setLoginStatus(status);
		userLoginInfoDao.persist(loginInfo);
	}
}
