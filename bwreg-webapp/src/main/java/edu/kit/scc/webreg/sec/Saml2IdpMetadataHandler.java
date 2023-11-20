/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.sec;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@ApplicationScoped
public class Saml2IdpMetadataHandler {

	@Inject
	private Logger logger;

	@Inject
	private SamlIdpConfigurationService idpConfigService;
	
	@Inject
	private SamlHelper samlHelper;
	
	
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<SamlIdpConfigurationEntity> idpConfigList = idpConfigService.findByHostname(request.getServerName());
		SamlIdpConfigurationEntity idpConfig = null;
		for (SamlIdpConfigurationEntity tempIdpConfig : idpConfigList) {
			try {
				URI uri = new URI(tempIdpConfig.getEntityId());
				if (request.getRequestURI().equals(uri.getPath())) {
					idpConfig = tempIdpConfig;
					break;
				}
			}
			catch (URISyntaxException e) {
				logger.warn("No a valid URI: {}", tempIdpConfig.getEntityId());
			}
		}
		
		if (idpConfig == null) {
			throw new ServletException("Unknown metadata uri");
		}
		
		response.setContentType("text/xml");
		PrintWriter w = response.getWriter();

		EntityDescriptor ed = samlHelper.create(EntityDescriptor.class, EntityDescriptor.DEFAULT_ELEMENT_NAME);
		ed.setEntityID(idpConfig.getEntityId());
		
		IDPSSODescriptor idpsso = samlHelper.create(IDPSSODescriptor.class, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
		idpsso.addSupportedProtocol(SAMLConstants.SAML20P_NS);
		ed.getRoleDescriptors().add(idpsso);
		
		X509Certificate x509cert = samlHelper.create(X509Certificate.class, X509Certificate.DEFAULT_ELEMENT_NAME);
		x509cert.setValue(idpConfig.getCertificate().replaceAll("-----(BEGIN|END) CERTIFICATE-----", ""));
		X509Data x509data = samlHelper.create(X509Data.class, X509Data.DEFAULT_ELEMENT_NAME);
		x509data.getX509Certificates().add(x509cert);
		KeyInfo keyInfo = samlHelper.create(KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME);
		keyInfo.getX509Datas().add(x509data);
		KeyDescriptor kd = samlHelper.create(KeyDescriptor.class, KeyDescriptor.DEFAULT_ELEMENT_NAME);
		kd.setUse(UsageType.SIGNING);
		kd.setKeyInfo(keyInfo);
		idpsso.getKeyDescriptors().add(kd);
		
		SingleSignOnService sso = samlHelper.create(SingleSignOnService.class, SingleSignOnService.DEFAULT_ELEMENT_NAME);
		sso.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		sso.setLocation("https://" + request.getServerName() + idpConfig.getRedirect());
		idpsso.getSingleSignOnServices().add(sso);
		
		w.print(samlHelper.prettyPrint(ed));
		w.close();
	}
}
