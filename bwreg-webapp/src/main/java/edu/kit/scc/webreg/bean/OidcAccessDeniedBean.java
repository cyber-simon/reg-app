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
package edu.kit.scc.webreg.bean;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.service.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.oidc.ServiceOidcClientService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class OidcAccessDeniedBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private IdentityEntity identity;
	private ServiceOidcClientEntity serviceOidcClient; 

	private Long id;
	
	private boolean initialized = false;
	
	private Boolean accessProblem = false;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
    @Inject 
    private SessionManager sessionManager;
    
    @Inject
    private IdentityService identityService;
    
	@Inject
	private KnowledgeSessionService knowledgeSessionService;

	@Inject
	private ServiceOidcClientService serviceOidcClientService;
	
    public void preRenderView(ComponentSystemEvent ev) {
    	if (! initialized) {
    		serviceOidcClient = serviceOidcClientService.fetch(getId());
    		identity = identityService.fetch(sessionManager.getIdentityId());
    		
    		checkServiceAccess();
    	}
	}

	private void checkServiceAccess() {
    		
		List<Object> objectList;
		
		BusinessRulePackageEntity rulePackage = serviceOidcClient.getRulePackage();

		if (rulePackage != null) {
			objectList = knowledgeSessionService.checkRule(rulePackage, identity);

			for (Object o : objectList) {
				if (o instanceof OverrideAccess) {
					return;
				}
			}

			for (Object o : objectList) {
				if (o instanceof UnauthorizedUser) {
					String s = ((UnauthorizedUser) o).getMessage();
		    		messageGenerator.addResolvedErrorMessage("reqs", "error", s, true);
		    		accessProblem = true;
				}
			}
		}
		
		if (serviceOidcClient.getScript() != null) {
			List<String> unauthorizedList = knowledgeSessionService.checkScriptAccess(serviceOidcClient.getScript(), identity);
			
			for (String s : unauthorizedList) {
	    		messageGenerator.addResolvedErrorMessage("reqs", "error", s, true);
	    		accessProblem = true;
			}
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getAccessProblem() {
		return accessProblem;
	}

	public ServiceOidcClientEntity getServiceOidcClient() {
		return serviceOidcClient;
	}
}
