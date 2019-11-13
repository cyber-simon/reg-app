package edu.kit.scc.webreg.service.saml;

import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.opensaml.saml.saml2.core.AuthnRequest;

import edu.kit.scc.webreg.dao.SamlAuthnRequestDao;
import edu.kit.scc.webreg.entity.SamlAuthnRequestEntity;

@Stateless
public class SamlIdpServiceImpl implements SamlIdpService {

	@Inject
	private SamlAuthnRequestDao samlAuthnRequestDao;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Override
	public long registerAuthnRequest(AuthnRequest authnRequest) {
		SamlAuthnRequestEntity authnRequestEntity = samlAuthnRequestDao.createNew();
		authnRequestEntity.setValidUntil(new Date(System.currentTimeMillis() + 30L * 60L * 1000L));
		authnRequestEntity.setAuthnrequestData(samlHelper.prettyPrint(authnRequest));
		authnRequestEntity = samlAuthnRequestDao.persist(authnRequestEntity);
		return authnRequestEntity.getId();
	}

	@Override
	public AuthnRequest resumeAuthnRequest(Long authnRequestId) {
		SamlAuthnRequestEntity authnRequestEntity = samlAuthnRequestDao.findById(authnRequestId);
		AuthnRequest authnRequest = samlHelper.unmarshal(authnRequestEntity.getAuthnrequestData(), AuthnRequest.class);
		return authnRequest;
	}
}
