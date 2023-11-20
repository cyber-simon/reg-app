package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.messaging.encoder.MessageEncodingException;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SamlSpRedirectService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject 
	private SamlIdpMetadataService idpService;
	 
	@Inject 
	private SamlSpConfigurationService spService;

	@Inject
	private Saml2RedirectService saml2RedirectService;

	@RetryTransaction
	public void redirectClient(Long idpEntityId, Long spEntityId,
			HttpServletRequest request, HttpServletResponse response)
			throws MessageEncodingException, ComponentInitializationException {
		
		SamlIdpMetadataEntity idpEntity = idpService.fetch(idpEntityId);
		SamlSpConfigurationEntity spEntity = spService.fetch(spEntityId);

		saml2RedirectService.redirectClient(idpEntity, spEntity, request, response);
	}
}
