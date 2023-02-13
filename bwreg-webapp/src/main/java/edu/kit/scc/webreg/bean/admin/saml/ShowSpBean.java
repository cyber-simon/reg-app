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

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity_;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@Named
@RequestScoped
public class ShowSpBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SamlSpMetadataService service;

	@Inject
	private SamlHelper samlHelper;

	private SamlSpMetadataEntity entity;

	private EntityDescriptor entityDescriptor;
	private SPSSODescriptor spssoDescriptor;

	private Map<KeyDescriptor, List<java.security.cert.X509Certificate>> certMap;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			certMap = new HashMap<KeyDescriptor, List<java.security.cert.X509Certificate>>();

			entity = service.findByIdWithAttrs(id, SamlSpMetadataEntity_.genericStore,
					SamlSpMetadataEntity_.federations);
			if (entity.getEntityDescriptor() != null) {
				entityDescriptor = samlHelper.unmarshal(entity.getEntityDescriptor(), EntityDescriptor.class);
				spssoDescriptor = (SPSSODescriptor) entityDescriptor
						.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).get(0);
			}
		}
	}

	public List<java.security.cert.X509Certificate> getCert(KeyDescriptor kd) {
		if (kd == null)
			return null;

		if (certMap.containsKey(kd))
			return certMap.get(kd);

		List<java.security.cert.X509Certificate> certList = new ArrayList<java.security.cert.X509Certificate>();
		KeyInfo keyInfo = kd.getKeyInfo();

		if (keyInfo == null)
			return null;

		for (X509Data x509 : keyInfo.getX509Datas()) {
			for (X509Certificate x509cert : x509.getX509Certificates()) {

				try {
					String certValue = x509cert.getValue();
					byte[] certBytes = Base64.decodeBase64(certValue.getBytes());
					java.security.cert.X509Certificate crt = (java.security.cert.X509Certificate) CertificateFactory
							.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBytes));
					certList.add(crt);
				} catch (Exception e) {
					String cause = "";
					if (e.getCause() != null)
						cause = e.getCause().getMessage();
					logger.warn("Unable to parse Certificate: " + e.toString() + " cause: " + cause);
				}
			}
		}

		certMap.put(kd, certList);

		return certList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SamlSpMetadataEntity getEntity() {
		return entity;
	}

	public void setEntity(SamlSpMetadataEntity entity) {
		this.entity = entity;
	}

	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

	public SPSSODescriptor getSpssoDescriptor() {
		return spssoDescriptor;
	}
}
