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
package edu.kit.scc.webreg.bean.admin.saml;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.X509Certificate;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.saml.CryptoHelper;

@Named
@ViewScoped
public class ShowSamlIdpConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ShowSamlIdpConfigurationBean.class);
	
	@Inject
	private SamlIdpConfigurationService service;

	@Inject 
	private CryptoHelper cryptoHelper;
	
	private SamlIdpConfigurationEntity entity;
	
	private Long id;

	private X509Certificate certificate;
	private X509Certificate standbyCertificate;
		
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "hostNameList");
			if (entity != null && entity.getCertificate() != null) {
				try {
					certificate = cryptoHelper.getCertificate(entity.getCertificate());
				} catch (IOException e) {
					logger.info("No valid X509 Cert", e);
					certificate = null;
				}
			}
			if (entity != null && entity.getStandbyCertificate() != null && 
					(! entity.getStandbyCertificate().equals(""))) {
				try {
					standbyCertificate = cryptoHelper.getCertificate(entity.getStandbyCertificate());
				} catch (IOException e) {
					logger.info("No valid X509 Cert", e);
					certificate = null;
				}
			}
		}
	}
	
	public SamlIdpConfigurationEntity getEntity() {
		return entity;
	}

	public void setEntity(SamlIdpConfigurationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public X509Certificate getCertificate() {
		return certificate;
	}

	public void setCertificate(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public X509Certificate getStandbyCertificate() {
		return standbyCertificate;
	}

	public void setStandbyCertificate(X509Certificate standbyCertificate) {
		this.standbyCertificate = standbyCertificate;
	}	
}
