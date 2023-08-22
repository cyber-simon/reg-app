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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.EmailTemplateService;
import edu.kit.scc.webreg.service.impl.KeyStoreService;
import edu.kit.scc.webreg.service.saml.CryptoHelper;

@Named
@ViewScoped
public class ShowEmailOverviewBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private EmailTemplateService emailTemplateService;

	@Inject
	private KeyStoreService keyStoreService;

	@Inject
	private CryptoHelper cryptoHelper;

	private LazyDataModel<EmailTemplateEntity> emailTemplates;

	public LazyDataModel<EmailTemplateEntity> getEmailTemplates() {
		if (emailTemplates == null) {
			emailTemplates = new GenericLazyDataModelImpl<>(emailTemplateService);
		}
		return emailTemplates;
	}

	public Boolean getHasSignatureKeys() {
		return keyStoreService.hasPrivateKeyEntry(KEYSTORE_CONTEXT_EMAIL, KEY_ALIAS_SIGNATURE);
	}

	public void downloadCertificates() throws IOException {
		String certificateChainPem = cryptoHelper.getPemString(getCertificateChain());
		try {
			sendFile(certificateChainPem, "fullchain.pem", "application/pem-certificate-chain");
		} catch (IOException e) {
			logger.error("An error has occured while downloading the certificates: " + e.getMessage(), e);
			throw e;
		}
	}

	private Certificate[] getCertificateChain() {
		try {
			return keyStoreService.fetchKeyStore(KEYSTORE_CONTEXT_EMAIL).getCertificateChain(KEY_ALIAS_SIGNATURE);
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Could not get certificate chain from key store", e);
		}
	}

	private void sendFile(String content, String filename, String contentType) throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();

		ExternalContext ec = fc.getExternalContext();
		ec.responseReset();
		ec.setResponseContentType(contentType);
		ec.setResponseContentLength(content.length());
		ec.setResponseHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		try (OutputStreamWriter writer = new OutputStreamWriter(ec.getResponseOutputStream(), StandardCharsets.UTF_8)) {
			writer.write(content);
		}

		fc.responseComplete();
	}

}
