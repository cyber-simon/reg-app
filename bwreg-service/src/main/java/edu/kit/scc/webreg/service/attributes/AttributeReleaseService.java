package edu.kit.scc.webreg.service.attributes;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.jpa.attribute.AttributeReleaseDao;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.service.attribute.release.AttributeReleaseHandler;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class AttributeReleaseService extends BaseServiceImpl<AttributeReleaseEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeReleaseDao dao;
	
	@Inject
	private AttributeReleaseHandler attributeReleaseHandler;

	public AttributeReleaseEntity calculateOidcValues(AttributeReleaseEntity attributeRelease, OidcFlowStateEntity flowState) {
		attributeRelease = dao.fetch(attributeRelease.getId());
		attributeReleaseHandler.calculateOidcValues(attributeRelease, flowState);
		return attributeRelease;
	}
	
	@Override
	protected BaseDao<AttributeReleaseEntity> getDao() {
		return dao;
	}
}
