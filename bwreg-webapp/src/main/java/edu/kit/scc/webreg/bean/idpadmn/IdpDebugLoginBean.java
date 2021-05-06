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
package edu.kit.scc.webreg.bean.idpadmn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class IdpDebugLoginBean implements Serializable {

 	private static final long serialVersionUID = 1L;

 	@Inject
 	private Logger logger;

	@Inject
	private FacesMessageGenerator messageGenerator;

 	@Inject
 	private SessionManager session;

	private String debugLog;
	private String extraRedirect;
	private HttpServletRequest request;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (FacesContext.getCurrentInstance().getExternalContext().getRequest() instanceof HttpServletRequest) {
			request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			if (request.getAttribute("_debugLog") != null) {
				debugLog = (String) request.getAttribute("_debugLog");
			}
			if (request.getAttribute("_debugLogExtraRedirect") != null) {
				extraRedirect = (String) request.getAttribute("_debugLogExtraRedirect");
			}	
		}
	}

	public void retryLogin() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/logout/local?redirect=idp_debug_login");
		} catch (IOException e) {
			logger.warn("Coud not redirect to page /logout/local?redirect=idp_debug_login: {}", e.getMessage());
		}
	}
	
	public String getDebugLog() {
		return debugLog;
	}

	public String getExtraRedirect() {
		return extraRedirect;
	}	
}
