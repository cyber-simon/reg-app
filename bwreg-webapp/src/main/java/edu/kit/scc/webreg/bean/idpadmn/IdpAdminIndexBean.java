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
package edu.kit.scc.webreg.bean.idpadmn;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.session.SessionManager;

@ManagedBean
@ViewScoped
public class IdpAdminIndexBean implements Serializable {

 	private static final long serialVersionUID = 1L;

 	@Inject
 	private Logger logger;
 	
 	@Inject
 	private SessionManager session;
 	
 	@Inject
 	private UserService userService;
 	
 	@Inject
 	private IdentityService identityService;
 	
 	@Inject
 	private SamlIdpMetadataService idpService;

	@Inject
	private SamlHelper samlHelper;

 	private IdentityEntity identity;
 	private List<UserEntity> userList;
 	private List<SamlIdpMetadataEntity> idpList;
 	
 	private SamlIdpMetadataEntity selectedIdp;
 	private SamlIdpMetadataEntity idp;
	private EntityDescriptor entityDescriptor;
	private IDPSSODescriptor idpssoDescriptor;

	private Map<KeyDescriptor, List<java.security.cert.X509Certificate>> certMap;
 	
	public void preRenderView(ComponentSystemEvent ev) {
		if (getIdpList().size() == 0) {
			throw new NotAuthorizedException("Not authorized");
		}
		
		selectedIdp = idpList.get(0);
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findById(session.getIdentityId());
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = new ArrayList<UserEntity>();
			for (UserEntity user : userService.findByIdentity(getIdentity())) {
				userList.add(userService.findByIdWithAttrs(user.getId(), "attributeStore"));
			}
		}
		return userList;
	}

	public List<SamlIdpMetadataEntity> getIdpList() {
		if (idpList == null) {
			idpList = new ArrayList<SamlIdpMetadataEntity>();
			for (UserEntity user : getUserList()) {
				if (user instanceof SamlUserEntity &&
						user.getAttributeStore().containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.7") &&
						user.getAttributeStore().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.7").contains("http://bwidm.scc.kit.edu/entitlement/idp-admin")) {
					idpList.add(((SamlUserEntity) user).getIdp());
				}
			}
		}		
		return idpList;
	}

	public SamlIdpMetadataEntity getSelectedIdp() {
		return selectedIdp;
	}

	public void setSelectedIdp(SamlIdpMetadataEntity selectedIdp) {
		this.selectedIdp = selectedIdp;
	}

	public SamlIdpMetadataEntity getIdp() {
		if (idp == null || (! idp.equals(selectedIdp))) {
			idp = idpService.findByIdWithAll(selectedIdp.getId());
			certMap = new HashMap<KeyDescriptor, List<java.security.cert.X509Certificate>>();
			
			entityDescriptor = samlHelper.unmarshal(idp.getEntityDescriptor(), EntityDescriptor.class);
			idpssoDescriptor = (IDPSSODescriptor) entityDescriptor.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).get(0);
			
		}
		return idp;
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
					java.security.cert.X509Certificate crt = (java.security.cert.X509Certificate) 
							CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certBytes));
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

	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

	public IDPSSODescriptor getIdpssoDescriptor() {
		return idpssoDescriptor;
	}
	
}
