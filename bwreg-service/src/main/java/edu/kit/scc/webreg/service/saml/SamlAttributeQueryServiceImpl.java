package edu.kit.scc.webreg.service.saml;

import java.io.IOException;

import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.soap.soap11.Envelope;
import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.saml.idp.AttributeAuthorityService;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.shibboleth.shared.component.ComponentInitializationException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SamlAttributeQueryServiceImpl implements SamlAttributeQueryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeAuthorityService aaService;
	
	@Inject
	private Saml2DecoderService saml2DecoderService;

	@Inject
	private SamlHelper samlHelper;

	@Override
	@RetryTransaction
	public void consumeAttributeQuery(HttpServletRequest request, HttpServletResponse response,
			SamlAAConfigurationEntity aaConfig) throws IOException {
		logger.debug("Consuming SAML AttributeQuery");

		try {
			AttributeQuery query = saml2DecoderService.decodeAttributeQuery(request);
			logger.debug("SAML AttributeQuery decoded");

			Envelope envelope = aaService.processAttributeQuery(aaConfig, query);

			response.getWriter().print(samlHelper.marshal(envelope));

		} catch (MessageDecodingException e) {
			logger.info("Could not execute AttributeQuery: {}", e.getMessage());
			sendErrorResponse(response, StatusCode.REQUEST_DENIED, e.getMessage());
		} catch (SecurityException e) {
			logger.info("Could not execute AttributeQuery: {}", e.getMessage());
			sendErrorResponse(response, StatusCode.REQUEST_DENIED, e.getMessage());
		} catch (SamlAuthenticationException e) {
			logger.info("Could not execute AttributeQuery: {}", e.getMessage());
			sendErrorResponse(response, StatusCode.REQUEST_DENIED, e.getMessage());
		} catch (ComponentInitializationException e) {
			logger.info("Could not execute AttributeQuery: {}", e.getMessage());
			sendErrorResponse(response, StatusCode.REQUEST_DENIED, e.getMessage());
		}
	}
	
	private void sendErrorResponse(HttpServletResponse response, String statusCodeString, String messageString)
			throws IOException {
		Envelope envelope = aaService.buildErrorResponse(statusCodeString, messageString);
		response.getWriter().print(samlHelper.marshal(envelope));
	}
}
