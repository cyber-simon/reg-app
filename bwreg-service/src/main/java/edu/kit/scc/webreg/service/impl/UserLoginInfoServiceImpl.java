package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.service.UserLoginInfoService;

@Stateless
public class UserLoginInfoServiceImpl extends BaseServiceImpl<UserLoginInfoEntity> implements UserLoginInfoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserLoginInfoDao dao;

	@Override
	public List<UserLoginInfoEntity> findByIdentity(Long identityId) {
		return dao.findAll(equal("identity.id", identityId));
	}

	@Override
	public void deleteLoginInfo(long millis) {
		Long start = System.currentTimeMillis();
		dao.deleteLoginInfo(millis);
		logger.info("Deleting old User Login Infos took {} ms", (System.currentTimeMillis() - start));
	}

	@Override
	protected BaseDao<UserLoginInfoEntity> getDao() {
		return dao;
	}
}
