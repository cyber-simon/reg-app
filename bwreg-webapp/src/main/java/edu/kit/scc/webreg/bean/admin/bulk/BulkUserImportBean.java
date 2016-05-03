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
package edu.kit.scc.webreg.bean.admin.bulk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserCreateService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@ManagedBean
@ViewScoped
public class BulkUserImportBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserService userService;

	@Inject
	private UserCreateService userCreateService;

	@Inject
	private AttributeQueryHelper attrQueryHelper;
	
	@Inject
	private Saml2AssertionService saml2AssertionService;

	@Inject
	private SamlHelper samlHelper;
		
	@Inject 
	private SamlIdpMetadataService idpService;
	
	@Inject 
	private SamlSpConfigurationService spService;

	private String uidField;
	private String salt;
	private String spEntityId;
	private String idpEntityId;
	
	private List<ImportUser> importUserList;

	private ImportUser[] selectedImport;
	
	public void fillTable() {
		importUserList = new ArrayList<ImportUser>();
		try {
			BufferedReader reader = new BufferedReader(new StringReader(uidField));
			String line;
			while ((line = reader.readLine()) != null) {
				String uid = line.trim();
				ImportUser importUser = new ImportUser();
				importUser.setUid(uid);
				importUser.setSpEntityId(spEntityId);
				importUser.setIdpEntityId(idpEntityId);
				importUser.generatePersistentId(salt);
				importUserList.add(importUser);
				logger.debug("Adding user {} for import: {}", uid, importUser.getPersistentId());
			}
			
		} catch (IOException e) {
			logger.error("StringReader broke down", e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgo arrrrrrrrr", e);
		} 
	}

	public void processSelected() {
		for (ImportUser importUser : selectedImport) {
			logger.debug("Processing user {} for import: {}", importUser.getUid(), importUser.getPersistentId());
			UserEntity userEntity = userService.findByPersistentWithRoles(importUser.getSpEntityId(), 
					importUser.getIdpEntityId(), importUser.getPersistentId());
			
			SamlSpConfigurationEntity spEntity = spService.findByEntityId(importUser.getSpEntityId());
			SamlIdpMetadataEntity idpEntity = idpService.findByEntityId(importUser.getIdpEntityId());

			if (userEntity != null) {
				logger.debug("User {} already in system", userEntity.getEppn());
				try {
					userService.updateUserFromIdp(userEntity, "bulk-import");
				} catch (UserUpdateException e) {
					logger.warn("AttributeQuery failed", e);
					importUser.setStatus("Fehler: " + e.getMessage());					
				}
			}
			else {
				logger.debug("User {} is new", importUser.getUid());

				EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(
						idpEntity.getEntityDescriptor(), EntityDescriptor.class);
				
				try {
					Map<String, List<Object>> attributeMap = new HashMap<String, List<Object>>();
					userEntity = userCreateService.preCreateUser(idpEntity, spEntity, importUser.getPersistentId(), null, attributeMap);

					Response samlResponse = attrQueryHelper.query(userEntity, idpEntity, idpEntityDescriptor, spEntity);
					
					Assertion assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor, spEntity);
	
					attributeMap = saml2AssertionService.extractAttributes(assertion);
					
					userEntity = userCreateService.createUser(userEntity, attributeMap, "bulk-import");
					logger.debug("Done Updating/Importing user {}", userEntity.getEppn());

					importUser.setStatus("Erfolgreich importiert/upgedated");
				}
				catch (Exception e) {
					logger.warn("AttributeQuery failed", e);
					importUser.setStatus("Fehler: " + e.getMessage());					
				}
			}
		}
	}
	
	public String getUidField() {
		return uidField;
	}

	public void setUidField(String uidField) {
		this.uidField = uidField;
	}

	public List<ImportUser> getImportUserList() {
		return importUserList;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getSpEntityId() {
		return spEntityId;
	}

	public void setSpEntityId(String spEntityId) {
		this.spEntityId = spEntityId;
	}

	public String getIdpEntityId() {
		return idpEntityId;
	}

	public void setIdpEntityId(String idpEntityId) {
		this.idpEntityId = idpEntityId;
	}

	public ImportUser[] getSelectedImport() {
		return selectedImport;
	}

	public void setSelectedImport(ImportUser[] selectedImport) {
		this.selectedImport = selectedImport;
	}
	
}
