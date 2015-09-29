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
package edu.kit.lsdf.sns;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.lsdf.sns.service.PFAccount;
import edu.kit.lsdf.sns.service.PFAccountService;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class SyncAndShareGuestBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	@Inject
	private SessionManager session;
	
	@Inject
	private ServiceService serviceService;
	
	@Inject
	private SamlIdpMetadataService idpService;
	
	@Inject
	private PFAccountService pfAccountService;
	
	@Inject
	private UserService userService;
	
	private ServiceEntity serviceEntity;
	
	private PFAccount pfAccount;
	
	private String serviceShortName;
	private String token;
	private String password1, password2;
	private String passwordRegex, passwordRegexMessage;
	private String entitlement;
	
	private Boolean touAccepted = false;
	
	private Boolean initialized = false;
	
    private List<PolicyEntity> policyList;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			serviceEntity = serviceService.findByShortName(serviceShortName);

			if (serviceEntity == null){
				messageGenerator.addResolvedErrorMessage("no-such-service");
				return;
			}

			serviceEntity = serviceService.findWithPolicies(serviceEntity.getId());
			
			policyList = new ArrayList<PolicyEntity>(serviceEntity.getPolicies());
			Collections.sort(policyList, new Comparator<PolicyEntity>() {
				@Override
				public int compare(PolicyEntity p1, PolicyEntity p2) {
					return p1.getId().compareTo(p2.getId());
				}
			});
			
			serviceEntity = serviceService.findByIdWithServiceProps(serviceEntity.getId());

			if (serviceEntity.getServiceProps().containsKey("password_regex"))
				passwordRegex = serviceEntity.getServiceProps().get("password_regex");
			if (serviceEntity.getServiceProps().containsKey("password_regex_message"))
				passwordRegexMessage = serviceEntity.getServiceProps().get("password_regex_message");

			if (serviceEntity.getServiceProps().containsKey("guest_entitlement")) 
				entitlement = serviceEntity.getServiceProps().get("guest_entitlement");
			
			if (session.isLoggedIn()) {
				UserEntity userEntity = userService.findByIdWithAll(session.getUserId());
				
				if (entitlement != null && userEntity.getAttributeStore() != null &&
						userEntity.getAttributeStore().containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.7") &&
						userEntity.getAttributeStore().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.7").matches(entitlement)) {
					messageGenerator.addResolvedWarningMessage("warning", "full-account-logged-in", true);
					return;
				}
			}

			if ((serviceShortName == null) || (token == null)){
				messageGenerator.addResolvedErrorMessage("use-link-from-email");
				return;
			}
			
			try {
				pfAccount = pfAccountService.findById(token, serviceEntity);
				
				if (pfAccount == null) {
					messageGenerator.addResolvedErrorMessage("unknown-token");
					return;
				}
				else if ((pfAccount.getCustom3() != null) && (pfAccount.getCustom3().equals("webreg-active"))) {
					messageGenerator.addResolvedErrorMessage("full-account-active");
					return;
				}
				else if ((pfAccount.getCustom3() != null) && (pfAccount.getCustom3().equals("webreg-inactive"))) {
					messageGenerator.addResolvedErrorMessage("full-account-inactive");
					return;
				}
				else if ((pfAccount.getCustom3() != null) && (pfAccount.getCustom3().equals("guest-active"))) {
					messageGenerator.addResolvedErrorMessage("guest-account-active");
					return;
				}

			} catch (RegisterException e) {
				messageGenerator.addErrorMessage(e.toString());
				return;
			}

			if (pfAccount.getUsername() != null && pfAccount.getUsername().contains("@")) {
				SamlIdpMetadataEntity idpEntity = idpService.findByScope(pfAccount.getUsername().split("@")[1]);
				if (idpEntity != null) {
					messageGenerator.addResolvedInfoMessage("scope_home_org", "scope_home_org_detail", true);
				}
			}
			
			password1 = null;
			password2 = null;

			initialized = true;
		}
	}

	public String save() {
		if (password1 == null || password2 == null ||
				"".equals(password1) || "".equals(password2)) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_empty", "password_field_empty_detail", true);
			return null;
		}
		else if (! password1.equals(password2)) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_different", "password_field_different_detail", true);
			return null;
		}

		if (passwordRegex != null &&
				(! password1.matches(passwordRegex))) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_complexity", passwordRegexMessage, false);
			return null;
		}
		
		if (! touAccepted) {
			messageGenerator.addResolvedWarningMessage("tou_error", "tou_not_accepted", "tou_not_accepted_detail", true);
			return null;
		}
		
		logger.info("Activating SNS guest {} ({}) now", pfAccount.getUsername(), pfAccount.getId());
		
		try {
			pfAccount.setPassword(password1);
			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
			pfAccount.setValidTil(dateFormat.format(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 50)));
			pfAccount.setCustom3("guest-active");
			pfAccount.setSpaceAllowed("0");
			pfAccount.setNotes("Accepted TOU in Webreg");
			pfAccountService.update(pfAccount, serviceEntity);
		} catch (RegisterException e) {
			messageGenerator.addErrorMessage(e.toString());
			return null;
		}
		
		return "/welcome/syncandshareguest-success.xhtml?faces-redirect=true";
	}
	
	public PFAccount getPfAccount() {
		return pfAccount;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}
	
	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}

	public Boolean getInitialized() {
		return initialized;
	}

	public Boolean getTouAccepted() {
		return touAccepted;
	}

	public void setTouAccepted(Boolean touAccepted) {
		this.touAccepted = touAccepted;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public List<PolicyEntity> getPolicyList() {
		return policyList;
	}

}
