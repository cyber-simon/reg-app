package edu.kit.scc.webreg.sec;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@Named
@WebServlet(urlPatterns = {"/saml/sp/metadata/*"})
public class SamlSpMetadataServlet implements Servlet {

	@Inject
	private Logger logger;
	
	@Inject
	private SamlSpConfigurationService spConfigService;

	@Inject
	private SamlHelper samlHelper;

	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String context = request.getServletContext().getContextPath();
		String path = request.getRequestURI().substring(
				context.length()).replaceAll("^/saml/sp/metadata", "");
		
		logger.debug("Dispatching saml sp metadata request context '{}' path '{}'", context, path);

		List<SamlSpConfigurationEntity> spConfigList = spConfigService.findByHostname(servletRequest.getServerName());
		SamlSpConfigurationEntity spConfig = null; 
		
		for (SamlSpConfigurationEntity s : spConfigList) {
			String entityPath = s.getEntityId().replaceAll("^https?://[^/]*", "");
			if (entityPath.equals(path)) {
				spConfig = s;
				break;
			}
		}
		
		if (spConfig == null) {
			logger.info("No matching saml sp metadata context '{}' path '{}'", context, path);
		}
		else {
			spConfig = spConfigService.findByIdWithAttrs(spConfig.getId(), "hostNameList");
			
			response.setContentType("text/xml");
			PrintWriter w = response.getWriter();

			EntityDescriptor ed = samlHelper.create(EntityDescriptor.class, EntityDescriptor.DEFAULT_ELEMENT_NAME);
			ed.setEntityID(spConfig.getEntityId());
			
			SPSSODescriptor spsso = samlHelper.create(SPSSODescriptor.class, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
			spsso.addSupportedProtocol(SAMLConstants.SAML20P_NS);
			ed.getRoleDescriptors().add(spsso);
			
			X509Certificate x509cert = samlHelper.create(X509Certificate.class, X509Certificate.DEFAULT_ELEMENT_NAME);
			x509cert.setValue(spConfig.getCertificate().replaceAll("-----(BEGIN|END) CERTIFICATE-----", ""));
			X509Data x509data = samlHelper.create(X509Data.class, X509Data.DEFAULT_ELEMENT_NAME);
			x509data.getX509Certificates().add(x509cert);
			KeyInfo keyInfo = samlHelper.create(KeyInfo.class, KeyInfo.DEFAULT_ELEMENT_NAME);
			keyInfo.getX509Datas().add(x509data);
			KeyDescriptor kd = samlHelper.create(KeyDescriptor.class, KeyDescriptor.DEFAULT_ELEMENT_NAME);
			kd.setUse(UsageType.SIGNING);
			kd.setKeyInfo(keyInfo);
			spsso.getKeyDescriptors().add(kd);
			
			List<String> hostNameList = new ArrayList<String>(spConfig.getHostNameList());
			Collections.sort(hostNameList);
			
			for (String hostName : hostNameList) {
				AssertionConsumerService acs = samlHelper.create(AssertionConsumerService.class, AssertionConsumerService.DEFAULT_ELEMENT_NAME);
				acs.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
				acs.setLocation("https://" + hostName + "/" + spConfig.getAcs());
				spsso.getAssertionConsumerServices().add(acs);
			}
			
			w.print(samlHelper.prettyPrint(ed));
			w.close();		
		}
	}
	
	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public String getServletInfo() {
		return null;
	}

	@Override
	public void destroy() {
	}	
}
