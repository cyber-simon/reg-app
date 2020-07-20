package edu.kit.scc.webreg.service.ssh;

import java.io.IOException;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

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
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
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
	public String authByUidNumberInteractive(ServiceEntity service, Long uidNumber, HttpServletRequest request)
			throws IOException, RestInterfaceException {
		
		service = serviceDao.merge(service);

		UserEntity user = userDao.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");
		
		if (service.getServiceProps().containsKey("twofa") && 
				service.getServiceProps().get("twofa").equalsIgnoreCase("enabled")) {
			
			UserLoginInfoEntity loginInfo = userLoginInfoDao.findByRegistryTwofaSuccess(registry.getId());
			
			if (loginInfo != null && loginInfo.getLoginStatus().equals(UserLoginInfoStatus.SUCCESS)) {
				
				// check expiry for twofa
				
				List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryDao.findByRegistryForInteractiveLogin(registry.getId());
				return buildKeyList(regKeyList, user);
			}
			else {
				return "";
			}
		}
		else {
			List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryDao.findByRegistryForInteractiveLogin(registry.getId());
			return buildKeyList(regKeyList, user);
		}
	}

	@Override
	public String authByUidNumberCommand(ServiceEntity service, Long uidNumber, HttpServletRequest request)
			throws IOException, RestInterfaceException {
		
		service = serviceDao.merge(service);

		UserEntity user = userDao.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");
		
		List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryDao.findByRegistryForCommandLogin(registry.getId());
		return buildKeyList(regKeyList, user);
	}

	@Override
	public String authByUidNumber(ServiceEntity service, Long uidNumber, HttpServletRequest request)
			throws IOException, RestInterfaceException {
		
		service = serviceDao.merge(service);

		UserEntity user = userDao.findByUidNumber(uidNumber);
		if (user == null)
			throw new NoUserFoundException("No such user");
		
		logger.debug("Searching for active registry for user {} and service {}", user.getId(), service.getShortName());
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null)
			throw new NoRegistryFoundException("No active registry for user");
		
		List<SshPubKeyRegistryEntity> regKeyList = sshPubKeyRegistryDao.findByRegistryForLogin(registry.getId());
		return buildKeyList(regKeyList, user);
	}

	protected String buildKeyList(List<SshPubKeyRegistryEntity> regKeyList, UserEntity user) {
		StringBuffer sb = new StringBuffer();
		for (SshPubKeyRegistryEntity regKey : regKeyList) {
			sb.append(regKey.getSshPubKey().getKeyType());
			sb.append(" ");
			sb.append(regKey.getSshPubKey().getEncodedKey());
			if (user.getEmail() != null) {
				sb.append(" ");
				sb.append(user.getEmail());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
