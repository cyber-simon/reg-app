package edu.kit.scc.webreg.service.identity;

import java.io.Serializable;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.UserProvisionerDao;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class UserProvisionerService extends BaseServiceImpl<UserProvisionerEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserProvisionerDao dao;
	
	@Override
	protected BaseDao<UserProvisionerEntity> getDao() {
		return dao;
	}
}
