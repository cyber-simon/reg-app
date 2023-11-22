/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.bean.admin.service;

import static edu.kit.scc.webreg.service.impl.KeyStoreService.KEYSTORE_CONTEXT_EMAIL;
import static edu.kit.scc.webreg.service.impl.KeyStoreService.KEY_ALIAS_SIGNATURE;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.commons.lang.StringUtils;

import edu.kit.scc.webreg.service.impl.KeyStoreService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class EditEmailSignatureBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private KeyStoreService keyStoreService;

	private String privateKeyPem = null;
	private String certificateChainPem = null;

	public String save() {
		keyStoreService.storePrivateKeyEntry(KEYSTORE_CONTEXT_EMAIL, KEY_ALIAS_SIGNATURE, privateKeyPem, certificateChainPem);
		privateKeyPem = null;
		certificateChainPem = null;
		return ViewIds.SHOW_EMAIL_OVERVIEW + "?faces-redirect=true";
	}

	public String cancel() {
		privateKeyPem = null;
		certificateChainPem = null;
		return ViewIds.SHOW_EMAIL_OVERVIEW + "?faces-redirect=true";
	}

	public Boolean getSaveDisabled() {
		return StringUtils.isEmpty(certificateChainPem) || StringUtils.isEmpty(privateKeyPem);
	}

	public String getPrivateKeyPem() {
		return privateKeyPem;
	}

	public void setPrivateKeyPem(String privateKeyPem) {
		this.privateKeyPem = privateKeyPem;
	}

	public String getCertificateChainPem() {
		return certificateChainPem;
	}

	public void setCertificateChainPem(String certificateChainPem) {
		this.certificateChainPem = certificateChainPem;
	}

}
