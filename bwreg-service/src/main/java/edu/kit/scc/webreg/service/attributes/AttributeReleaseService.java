package edu.kit.scc.webreg.service.attributes;

import java.util.Date;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.jpa.attribute.AttributeReleaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcFlowStateDao;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.ReleaseStatusType;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
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
	OidcFlowStateDao flowStateDao;
	
	@Inject
	private AttributeReleaseHandler attributeReleaseHandler;

	public AttributeReleaseEntity calculateOidcValues(AttributeReleaseEntity attributeRelease, OidcFlowStateEntity flowState) {
		attributeRelease = dao.fetch(attributeRelease.getId());
		attributeReleaseHandler.calculateOidcValues(attributeRelease, flowState);
		return attributeRelease;
	}
	
	public AttributeReleaseEntity accept(AttributeReleaseEntity attributeRelease, OidcFlowStateEntity flowState, IdentityEntity identity) {
		attributeRelease = dao.fetch(attributeRelease.getId());
		attributeRelease.setReleaseStatus(ReleaseStatusType.GOOD);
		attributeRelease.setIssuedAt(new Date());
		
		flowState = flowStateDao.fetch(flowState.getId());
		flowState.setValidUntil(new Date(System.currentTimeMillis() + (10L * 60L * 1000L)));
		flowState.setIdentity(identity);
		flowState.setAttributeRelease(attributeRelease);
		
		return attributeRelease;
	}
	
	public AttributeReleaseEntity reject(AttributeReleaseEntity attributeRelease, OidcFlowStateEntity flowState, IdentityEntity identity) {
		attributeRelease = dao.fetch(attributeRelease.getId());
		attributeRelease.setReleaseStatus(ReleaseStatusType.REJECTED);
		attributeRelease.setIssuedAt(new Date());
		
		flowState = flowStateDao.fetch(flowState.getId());
		flowState.setValidUntil(new Date(System.currentTimeMillis() + (10L * 60L * 1000L)));
		flowState.setIdentity(identity);
		flowState.setAttributeRelease(attributeRelease);
		
		return attributeRelease;
	}
	
	@Override
	protected BaseDao<AttributeReleaseEntity> getDao() {
		return dao;
	}
}
