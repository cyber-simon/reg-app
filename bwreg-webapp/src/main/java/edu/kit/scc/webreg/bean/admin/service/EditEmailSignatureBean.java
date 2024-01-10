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
public class EditEmailSignatureBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private KeyStoreService keyStoreService;

	private String alias = null;
	private String newAlias = null;

	public String save() {
		keyStoreService.renamePrivateKeyEntry(KEYSTORE_CONTEXT_EMAIL, alias, newAlias);
		alias = null;
		return ViewIds.SHOW_EMAIL_OVERVIEW + "?faces-redirect=true";
	}

	public String cancel() {
		alias = null;
		return ViewIds.SHOW_EMAIL_OVERVIEW + "?faces-redirect=true";
	}

	public String getNewAlias() {
		return newAlias;
	}

	public void setNewAlias(String newAlias) {
		this.newAlias = newAlias;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
