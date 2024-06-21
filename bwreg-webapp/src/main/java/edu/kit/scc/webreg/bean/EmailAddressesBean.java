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

import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity_;
import edu.kit.scc.webreg.exc.VerificationException;
import edu.kit.scc.webreg.service.identity.IdentityEmailAddressService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.internet.AddressException;
import jakarta.validation.constraints.Email;

@Named
@ViewScoped
public class EmailAddressesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private IdentityService identityService;

	@Inject
	private IdentityEmailAddressService service;

	@Inject
	private FacesMessageGenerator messageGenerator;

	private IdentityEntity identity;

	@Email
	private String addEmailAddress;

	private String token;
	
	public void preRenderView(ComponentSystemEvent ev) {
	}

	public void addEmailAddress() {
		try {
			service.addEmailAddress(getIdentity(), addEmailAddress, "idty-" + session.getIdentityId());
			setAddEmailAddress(null);
			identity = null;
		} catch (AddressException e) {
			messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", e.toString());
		}
	}

	public void deleteEmailAddress(IdentityEmailAddressEntity address) {
		service.deleteEmailAddress(address, "idty-" + session.getIdentityId());
		identity = null;
	}
	
	public void checkVerification() {
		try {
			service.checkVerification(getIdentity(), getToken(), "idty-" + session.getIdentityId());
			token = null;
			identity = null;
		} catch (VerificationException e) {
			messageGenerator.addResolvedErrorMessage("email_addresses." + e.getMessage());
		}
	}
	
	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findByIdWithAttrs(session.getIdentityId(), IdentityEntity_.emailAddresses);
		}
		return identity;
	}

	public String getAddEmailAddress() {
		return addEmailAddress;
	}

	public void setAddEmailAddress(String addEmailAddress) {
		this.addEmailAddress = addEmailAddress;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
