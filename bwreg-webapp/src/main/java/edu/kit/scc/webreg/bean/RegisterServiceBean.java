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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.AttributeSourceQueryService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class RegisterServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(RegisterServiceBean.class);
	
	private UserEntity user;

	private ServiceEntity service;
	
	private Long id;
	
	private String serviceShortName;
	
	private Boolean initialzed = false;
	
	private Boolean errorState = false;

	private Boolean accessAllowed;
	
	private List<String> requirementsList;
	
    @Inject
    private ServiceService serviceService;

    @Inject
    private UserService userService;
    
    @Inject
    private RegistryService registryService;
    
    @Inject
    private RegisterUserService registerUserService;
    
    @Inject 
    private SessionManager sessionManager;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject
	private AttributeSourceQueryService asQueryService;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
    private List<RegisterServiceBean.PolicyHolder> policyHolderList;
    
	public void preRenderView(ComponentSystemEvent ev) {
    	user = userService.findById(sessionManager.getUserId());

    	if (! initialzed) {
    		if (id == null && serviceShortName != null) {
    			service = serviceService.findByShortName(serviceShortName);
    			id = service.getId();
    		}
    		
			service = serviceService.findByIdWithAttrs(id, "policies", "attributeSourceService", "serviceProps");
			
			List<RegistryEntity> r = registryService.findByServiceAndUserAndNotStatus(service, user, 
					RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
			if (r.size() != 0) {
				errorState = true;
	    		messageGenerator.addResolvedErrorMessage("errorState", "error", "already_registered", true);
				return;
			}
			
			for (AttributeSourceServiceEntity asse : service.getAttributeSourceService()) {
				logger.info("Updating attribute source {}", asse.getAttributeSource().getName());
				try {
					asQueryService.updateUserAttributes(user, asse.getAttributeSource(), "user-" + user.getId());
				} catch (UserUpdateException e) {
					logger.info("Updating attribute source exception", e);
				}
			}
			
			policyHolderList = new ArrayList<RegisterServiceBean.PolicyHolder>();
			
			List<PolicyEntity> policiesTemp = new ArrayList<PolicyEntity>(service.getPolicies());
			Collections.sort(policiesTemp, new Comparator<PolicyEntity>() {
				@Override
				public int compare(PolicyEntity p1, PolicyEntity p2) {
					return p1.getId().compareTo(p2.getId());
				}
			});
			
			for (PolicyEntity policy : policiesTemp) {
				PolicyHolder ph = new PolicyHolder();
				ph.setPolicy(policy);
				ph.setChecked(false);
				policyHolderList.add(ph);
			}

			if (! registerUserService.checkWorkflow(service.getRegisterBean()))
				throw new MisconfiguredServiceException("Der Registrierungsprozess für den Dienst ist nicht korrekt konfiguriert");

    		
			List<Object> objectList;
			
			if (service.getAccessRule() == null) {
				objectList = knowledgeSessionService.checkRule("default", "permitAllRule", "1.0.0", user, service, null, "user-self", false);
			}
			else {
				BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();
				if (rulePackage != null) {
					objectList = knowledgeSessionService.checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
						rulePackage.getKnowledgeBaseVersion(), user, service, null, "user-self", false);
				}
				else {
					throw new IllegalStateException("checkServiceAccess called with a rule (" +
								service.getAccessRule().getName() + ") that has no rulePackage");
				}
			}

			requirementsList = new ArrayList<String>();
			for (Object o : objectList) {
				if (o instanceof OverrideAccess) {
					requirementsList.clear();
					logger.debug("Removing requirements due to OverrideAccess");
					break;
				}
				else if (o instanceof UnauthorizedUser) {
					String s = ((UnauthorizedUser) o).getMessage();
					requirementsList.add(s);
				}
			}

			if (requirementsList.size() == 0) {
				accessAllowed = true;
			}
			else {
				accessAllowed = false;
			}

			for (String s : requirementsList) {
	    		messageGenerator.addResolvedErrorMessage("reqs", "error", s, true);
			}
			
			initialzed = true;
		}
	}

    public String registerUser() {

    	if (! accessAllowed) {
    		messageGenerator.addErrorMessage("need_check", "Zugangsvorraussetzungen!", "Sie erfüllen nicht alle Zugangsvorraussetzungen.");
			return null;
    	}
    	
    	logger.debug("testing all checkboxes");
    	
    	for(PolicyHolder ph : policyHolderList) {
    		if (! ph.getChecked()) {
        		messageGenerator.addWarningMessage("need_check", "Zustimmung fehlt!", "Sie müssen allen Nutzungbedingungen zustimmen.");
    			return null;
    		}
    	}
    	
    	logger.debug("user {} wants to register to service {} using bean {}", new Object[] {
    			user.getEppn(), service.getName(), service.getRegisterBean()
    	});
    	
    	try {
    		registerUserService.registerUser(user, service, "user-self");
    		sessionManager.setUnregisteredServiceCreated(null);
    	} catch (RegisterException e) {
			FacesContext.getCurrentInstance().addMessage("need_check", 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Registrierung fehlgeschlagen", e.getMessage()));
    		logger.warn("Register failed!", e);
			return null;
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage("need_check", 
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Registrierung fehlgeschlagen", e.getMessage()));
    		logger.warn("Register failed!", e);
			return null;
		}
  	
    	if (service.getServiceProps().containsKey("redirect_after_register")) {
    		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    		try {
				context.redirect(service.getServiceProps().get("redirect_after_register"));
				return null;
			} catch (IOException e) {
				logger.info("Could not redirect client", e);
			}
    	}
    	
    	return ViewIds.INDEX_USER + "?faces-redirect=true";
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

	public List<RegisterServiceBean.PolicyHolder> getPolicyHolderList() {
		return policyHolderList;
	}

	public Boolean getAccessAllowed() {
		return accessAllowed;
	}

	public List<String> getRequirementsList() {
		return requirementsList;
	}

	public class PolicyHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		private PolicyEntity policy;
		private Boolean checked;
		
		public PolicyEntity getPolicy() {
			return policy;
		}
		
		public void setPolicy(PolicyEntity policy) {
			this.policy = policy;
		}
		
		public Boolean getChecked() {
			return checked;
		}
		
		public void setChecked(Boolean checked) {
			this.checked = checked;
		}
	}

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}

	public Boolean getErrorState() {
		return errorState;
	}

	public void setErrorState(Boolean errorState) {
		this.errorState = errorState;
	}
}
