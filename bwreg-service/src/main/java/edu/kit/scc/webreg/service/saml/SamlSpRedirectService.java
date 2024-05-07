package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;

import org.opensaml.messaging.encoder.MessageEncodingException;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.shibboleth.shared.component.ComponentInitializationException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SamlSpRedirectService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject 
	private SamlIdpMetadataDao idpDao;
	 
	@Inject 
	private SamlSpConfigurationDao spDao;

	@Inject
	private Saml2RedirectService saml2RedirectService;

	@RetryTransaction
	public void redirectClient(Long idpEntityId, Long spEntityId,
			HttpServletRequest request, HttpServletResponse response)
			throws MessageEncodingException, ComponentInitializationException {
		
		SamlIdpMetadataEntity idpEntity = idpDao.fetch(idpEntityId);
		SamlSpConfigurationEntity spEntity = spDao.fetch(spEntityId);

		saml2RedirectService.redirectClient(idpEntity, spEntity, request, response);
	}
}
