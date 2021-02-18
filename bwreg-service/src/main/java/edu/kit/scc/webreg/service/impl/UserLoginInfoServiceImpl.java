package edu.kit.scc.webreg.service.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.service.UserLoginInfoService;

@Stateless
public class UserLoginInfoServiceImpl extends BaseServiceImpl<UserLoginInfoEntity, Long> implements UserLoginInfoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserLoginInfoDao dao;

	@Override
	public void deleteLoginInfo(long millis) {
		Long start = System.currentTimeMillis();
		dao.deleteLoginInfo(millis);
		logger.info("Deleting old User Login Infos took {} ms", (System.currentTimeMillis() - start));
	}

	@Override
	protected BaseDao<UserLoginInfoEntity, Long> getDao() {
		return dao;
	}
}
