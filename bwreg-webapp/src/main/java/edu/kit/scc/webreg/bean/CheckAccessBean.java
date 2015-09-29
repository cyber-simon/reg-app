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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class CheckAccessBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private UserEntity user;
	private ServiceEntity service;
	private RegistryEntity registry;

	private Long id;
	
	private boolean initialized = false;
	
	private Boolean accessProblem = false;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
    @Inject
    private RegistryService registryService;

    @Inject 
    private SessionManager sessionManager;
    
    @Inject
    private UserService userService;
    
	@Inject
	private KnowledgeSessionService knowledgeSessionService;

    public void preRenderView(ComponentSystemEvent ev) {
    	if (! initialized) {
        	user = userService.findById(sessionManager.getUserId());
        	registry = registryService.findById(id);
        	
        	if (! registry.getUser().getId().equals(user.getId())) {
        		throw new NotAuthorizedException("no authorized to view this item");
        	}
        	
        	service = registry.getService();
        	
       		checkServiceAccess(registry.getService());    		
    	}
	}

	private void checkServiceAccess(ServiceEntity service) {
    		
		List<Object> objectList;
		
		if (service.getAccessRule() == null) {
			objectList = knowledgeSessionService.checkRule("default", "permitAllRule", "1.0.0", user, service, registry, "user-self", false);
		}
		else {
			BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();

			if (rulePackage != null) {
				objectList = knowledgeSessionService.checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
					rulePackage.getKnowledgeBaseVersion(), user, service, registry, "user-self", false);
			}
			else {
				throw new IllegalStateException("checkServiceAccess called with a rule (" +
							service.getAccessRule().getName() + ") that has no rulePackage");
			}
		}

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

	public UserEntity getUser() {
		return user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ServiceEntity getService() {
		return service;
	}

	public RegistryEntity getRegistry() {
		return registry;
	}

	public Boolean getAccessProblem() {
		return accessProblem;
	}
}
