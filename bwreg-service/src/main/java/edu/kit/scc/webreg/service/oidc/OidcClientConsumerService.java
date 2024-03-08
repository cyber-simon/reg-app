package edu.kit.scc.webreg.service.oidc;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.jpa.oidc.OidcClientConsumerDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConsumerEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class OidcClientConsumerService extends BaseServiceImpl<OidcClientConsumerEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcClientConsumerDao dao;

	@Override
	protected BaseDao<OidcClientConsumerEntity> getDao() {
		return dao;
	}
}
