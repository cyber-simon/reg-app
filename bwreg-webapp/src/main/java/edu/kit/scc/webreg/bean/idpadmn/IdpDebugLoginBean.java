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

import java.io.IOException;
import java.io.Serializable;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

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
