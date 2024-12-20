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

import java.io.Serializable;

import edu.kit.scc.webreg.service.impl.KeyStoreService;
import edu.kit.scc.webreg.util.ViewIds;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class DeleteEmailSignatureBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private KeyStoreService keyStoreService;

	private String alias;
	
	public String delete() {
		keyStoreService.deletePrivateKeyEntry(KEYSTORE_CONTEXT_EMAIL, alias);
		return ViewIds.SHOW_EMAIL_OVERVIEW + "?faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.SHOW_EMAIL_OVERVIEW + "?faces-redirect=true";
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
