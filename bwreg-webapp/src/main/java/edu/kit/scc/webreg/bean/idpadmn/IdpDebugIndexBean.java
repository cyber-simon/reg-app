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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class IdpDebugIndexBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private FacesMessageGenerator messageGenerator;

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

	private UserEntity selectedUser;
	private SamlIdpMetadataEntity idp;
	private EntityDescriptor entityDescriptor;
	private IDPSSODescriptor idpssoDescriptor;

	private String debugLog;

	private Map<KeyDescriptor, List<java.security.cert.X509Certificate>> certMap;

	public void preRenderView(ComponentSystemEvent ev) {
		if (selectedUser == null) {
			selectedUser = getUserList().get(0);
		}
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.fetch(session.getIdentityId());
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = new ArrayList<UserEntity>();
			for (UserEntity user : userService.findByIdentity(getIdentity())) {
				userList.add(
						userService.findByIdWithAttrs(user.getId(), UserEntity_.attributeStore, UserEntity_.roles,
								SamlUserEntity_.idp));
			}
		}
		return userList;
	}

	public List<SamlIdpMetadataEntity> getIdpList() {
		if (idpList == null) {
			idpList = new ArrayList<SamlIdpMetadataEntity>();
			for (UserEntity user : getUserList()) {
				if (user instanceof SamlUserEntity) {
					idpList.add(((SamlUserEntity) user).getIdp());
				}
			}
		}
		return idpList;
	}

	public SamlIdpMetadataEntity getIdp() {
		if (idp == null) {
			if (getSelectedUser() instanceof SamlUserEntity) {
				idp = idpService.findByIdWithAll(((SamlUserEntity) getSelectedUser()).getIdp().getId());
				certMap = new HashMap<KeyDescriptor, List<java.security.cert.X509Certificate>>();
				entityDescriptor = samlHelper.unmarshal(idp.getEntityDescriptor(), EntityDescriptor.class);
				idpssoDescriptor = (IDPSSODescriptor) entityDescriptor
						.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).get(0);
			}
		} else {
			if (getSelectedUser() instanceof SamlUserEntity) {
				SamlUserEntity samlUser = (SamlUserEntity) getSelectedUser();
				if (!samlUser.getIdp().equals(idp)) {
					idp = idpService.findByIdWithAll(samlUser.getIdp().getId());
					certMap = new HashMap<KeyDescriptor, List<java.security.cert.X509Certificate>>();
					entityDescriptor = samlHelper.unmarshal(idp.getEntityDescriptor(), EntityDescriptor.class);
					idpssoDescriptor = (IDPSSODescriptor) entityDescriptor
							.getRoleDescriptors(IDPSSODescriptor.DEFAULT_ELEMENT_NAME).get(0);
				}
			} else {
				idp = null;
			}
		}

		return idp;
	}

	public void updateFromIdp() {
		logger.info("Trying user update for {}", getSelectedUser().getEppn());

		if (getSelectedUser() instanceof SamlUserEntity) {
			try {
				StringBuffer sb = new StringBuffer();
				userService.updateUserFromIdp((SamlUserEntity) getSelectedUser(), "user-" + session.getIdentityId(),
						sb);
				messageGenerator.addInfoMessage("Info", "SAML AttributeQuery went through without errors");
				debugLog = sb.toString();
			} catch (UserUpdateException e) {
				logger.info("Exception while Querying IDP: {}", e.getMessage());
				String extendedInfo = "";
				if (e.getCause() != null) {
					logger.info("Cause is: {}", e.getCause().getMessage());
					extendedInfo = "<br/>Cause: " + e.getCause().getMessage();
					if (e.getCause().getCause() != null) {
						logger.info("Inner Cause is: {}", e.getCause().getCause().getMessage());
						extendedInfo = "<br/>Inner Cause: " + e.getCause().getCause().getMessage();
					}
				}
				messageGenerator.addErrorMessage("Problem",
						"Exception while Querying IDP: " + e.getMessage() + extendedInfo);
			}
		} else {
			logger.info("No update method available for class {}", getSelectedUser().getClass().getName());
			messageGenerator.addErrorMessage("Problem",
					"No update method available for class " + getSelectedUser().getClass().getName());
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

	public EntityDescriptor getEntityDescriptor() {
		return entityDescriptor;
	}

	public IDPSSODescriptor getIdpssoDescriptor() {
		return idpssoDescriptor;
	}

	public UserEntity getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserEntity selectedUser) {
		this.selectedUser = selectedUser;
	}

	public String getDebugLog() {
		return debugLog;
	}

}
