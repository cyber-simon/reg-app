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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

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
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.PolicyService;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.reg.AttributeSourceQueryService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.token.GenericTwoFaToken;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class RegisterServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(RegisterServiceBean.class);
	
	private IdentityEntity identity;
	private List<UserEntity> userList;
	private UserEntity selectedUserEntity;
	
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
    private IdentityService identityService;
    
    @Inject
    private UserService userService;
    
    @Inject
    private RegistryService registryService;
    
    @Inject
    private RegisterUserService registerUserService;
    
    @Inject
    private PolicyService policyService;
    
    @Inject 
    private SessionManager sessionManager;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject
	private AttributeSourceQueryService asQueryService;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	@Inject
	private TwoFaService twoFaService;
	
    private List<RegisterServiceBean.PolicyHolder> policyHolderList;
    
	public void preRenderView(ComponentSystemEvent ev) {
		
		if (getUserList().size() == 1) {
			selectedUserEntity = getUserList().get(0);
		}
		else {
			selectedUserEntity = getIdentity().getPrefUser();
		}
		
    	if (! initialzed) {
    		if (id == null && serviceShortName != null) {
    			service = serviceService.findByShortName(serviceShortName);
    			id = service.getId();
    		}
    		
			service = serviceService.findByIdWithAttrs(id, "attributeSourceService", "serviceProps");
			
			List<RegistryEntity> r = registryService.findByServiceAndIdentityAndNotStatus(service, getIdentity(), 
					RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
			if (r.size() != 0) {
				errorState = true;
	    		messageGenerator.addResolvedErrorMessage("errorState", "error", "already_registered", true);
				return;
			}
			
			if (identity.getRegistrationLock() != null && identity.getRegistrationLock().after(new Date(System.currentTimeMillis() - (5 * 60 * 1000L)))) {
				// There is a registration running and a timeout is not reached
				errorState = true;
	    		messageGenerator.addResolvedErrorMessage("errorState", "error", "registration_already_running", true);
	    		logger.warn("Identity {} cannot register for service {}: There is a registration already running.", identity.getId(), service.getId());
				return;
			}
			
			for (UserEntity user : userList) {
				for (AttributeSourceServiceEntity asse : service.getAttributeSourceService()) {
					logger.info("Updating attribute source {}", asse.getAttributeSource().getName());
					try {
						asQueryService.updateUserAttributes(user, asse.getAttributeSource(), "user-" + user.getId());
					} catch (UserUpdateException e) {
						logger.info("Updating attribute source exception", e);
					}
				}
			}
			
			policyHolderList = new ArrayList<RegisterServiceBean.PolicyHolder>();
			
			List<PolicyEntity> policiesTemp = policyService.resolvePoliciesForService(service, selectedUserEntity);
			
			for (PolicyEntity policy : policiesTemp) {
				PolicyHolder ph = new PolicyHolder();
				ph.setPolicy(policy);
				ph.setChecked(false);
				policyHolderList.add(ph);
			}

			if (! registerUserService.checkWorkflow(service.getRegisterBean()))
				throw new MisconfiguredServiceException("Der Registrierungsprozess für den Dienst ist nicht korrekt konfiguriert");

    		checkUserAccess();
			
			initialzed = true;
		}
	}

	public void checkUserAccess() {
		List<Object> objectList;
		
		if (service.getAccessRule() == null) {
			objectList = knowledgeSessionService.checkRule("default", "permitAllRule", "1.0.0", selectedUserEntity, service, null, "user-self", false);
		}
		else {
			BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();
			if (rulePackage != null) {
				objectList = knowledgeSessionService.checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
					rulePackage.getKnowledgeBaseVersion(), selectedUserEntity, service, null, "user-self", false);
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
		

		if (service.getServiceProps().containsKey("twofa") && 
				(service.getServiceProps().get("twofa").equalsIgnoreCase("enabled") 
					|| service.getServiceProps().get("twofa").equalsIgnoreCase("enabled_twostep"))) {
			/*
			 * second factor for service is enabled. Check if user has registered second factor
			 */
			try {
				TwoFaTokenList tokenList = twoFaService.findByIdentity(getIdentity());

				Map<String, Object> rendererContext = new HashMap<String, Object>();
				rendererContext.put("service", service);
				
				if (tokenList.getReallyReadOnly() != null && tokenList.getReallyReadOnly()) {
					// 2fa are managed by other org, we can not see if the user has an active token
				}
				else if (tokenList.size() == 0) {
					accessAllowed = false;
		    		messageGenerator.addResolvedMessage("reqs", FacesMessage.SEVERITY_ERROR, "error", 
		    				"twofa_mandatory", true, rendererContext);
				}
				else {
					Boolean noActive = true;
					for (GenericTwoFaToken lt : tokenList) {
						if (lt.getIsactive()) {
							noActive = false;
							break;
						}
					}
					
					if (noActive) {
						accessAllowed = false;
			    		messageGenerator.addResolvedMessage("reqs", FacesMessage.SEVERITY_ERROR, "error", 
			    				"twofa_mandatory", true, rendererContext);
					}
				}
			} catch (TwoFaException e) {
				logger.warn("There is a problem communicating with twofa server" + e.getMessage());
				errorState = true;
	    		messageGenerator.addResolvedErrorMessage("errorState", "error", "twofa_problem", true);
				return;
			}
		}		
	}
	
    public String registerUser() {

		checkUserAccess();

    	if (! accessAllowed) {
    		messageGenerator.addErrorMessage("need_check", "Zugangsvorraussetzungen!", "Sie erfüllen nicht alle Zugangsvorraussetzungen.");
			return null;
    	}

		List<RegistryEntity> r = registryService.findByServiceAndIdentityAndNotStatus(service, getIdentity(), 
				RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
		if (r.size() != 0) {
    		messageGenerator.addResolvedErrorMessage("errorState", "error", "already_registered", true);
			return null;
		}
    	
    	logger.debug("testing all checkboxes");
    	
    	for(PolicyHolder ph : policyHolderList) {
    		if (ph.getPolicy() != null && ph.getPolicy().getShowOnly() != null && ph.getPolicy().getShowOnly()) {
    			logger.debug("Policy {} in Service {} is just for show", ph.getPolicy().getId(), service.getId());
    		}
    		else if (! ph.getChecked()) {
    			logger.debug("Policy {} in Service {} is not checked", ph.getPolicy().getId(), service.getId());
        		messageGenerator.addWarningMessage("need_check", "Zustimmung fehlt!", "Sie müssen allen Nutzungbedingungen zustimmen.");
    			return null;
    		}
    		else {
    			logger.debug("Policy {} in Service {} is checked", ph.getPolicy().getId(), service.getId());
    		}
    	}
    	
    	logger.debug("identity {} with user {} wants to register to service {} using bean {}", new Object[] {
    			getIdentity().getId(), selectedUserEntity.getEppn(), service.getName(), service.getRegisterBean()
    	});
    	
    	RegistryEntity registry;
    	
    	try {
			//
			// Insert/check gate to prevent double registrations per identity in this place 
			//
    		
			identity = identityService.findById(sessionManager.getIdentityId());
			if (identity.getRegistrationLock() != null && 
					(identity.getRegistrationLock().getTime() < System.currentTimeMillis() - (5 * 60 * 1000L))) {
				// There is a registration running and a timeout is not reached
	    		messageGenerator.addResolvedErrorMessage("errorState", "error", "registration_already_running", true);
	    		logger.warn("Identity {} cannot register for service {}: There is a registration already running.", identity.getId(), service.getId());
				return null;   				
			}
			else {
				identity.setRegistrationLock(new Date());
				identity = identityService.save(identity);

	    		if (policyHolderList.size() == 0) {
	    			registry = registerUserService.registerUser(selectedUserEntity, service, "user-self");
	    		}
	    		else {
	    			List<Long> policyIdList = new ArrayList<Long>();
	    			for (PolicyHolder ph : policyHolderList) {
	    				policyIdList.add(ph.getPolicy().getId());
	    			}
	
    				registry = registerUserService.registerUser(selectedUserEntity, service, policyIdList, "user-self");
	    		}

	    		//
	    		// remove double registration lock
	    		//
	    		
	    		identity.setRegistrationLock(null);
				identity = identityService.save(identity);
			}
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
  	
		try {
	    	if (service.getServiceProps().containsKey("redirect_after_register")) {
	    		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
				context.redirect(service.getServiceProps().get("redirect_after_register"));
				sessionManager.setOriginalRequestPath(null);
				return null;
	    	}
	    	else if (service.getPasswordCapable() &&
	    			service.getServiceProps().containsKey("ecp") &&
	    			service.getServiceProps().get("ecp").equalsIgnoreCase("disabled")) {
	    		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
				context.redirect("../service/set-password.xhtml?registryId=" + registry.getId() + "&no=created");
				return null;
	    	}
	    	else if (sessionManager.getOriginalRequestPath() != null) {
	    		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
				context.redirect(sessionManager.getOriginalRequestPath());
				sessionManager.setOriginalRequestPath(null);
				return null;
	    	}

			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.redirect("../service/registry-detail.xhtml?regId=" + registry.getId());

		} catch (IOException e) {
			logger.info("Could not redirect client", e);
		}

    	return null;
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

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findById(sessionManager.getIdentityId());
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = userService.findByIdentity(getIdentity());
		}
		return userList;
	}

	public UserEntity getSelectedUserEntity() {
		return selectedUserEntity;
	}

	public void setSelectedUserEntity(UserEntity selectedUserEntity) {
		this.selectedUserEntity = selectedUserEntity;
	}
}
