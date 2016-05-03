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
package edu.kit.scc.webreg.bean.admin;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlAAMetadataService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@ManagedBean
@ViewScoped
public class AttributeQueryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeQueryHelper attrQueryHelper;
	
	@Inject
	private Saml2AssertionService saml2AssertionService;

	@Inject
	private SamlHelper samlHelper;
		
	@Inject 
	private SamlIdpMetadataService idpService;
	
	@Inject 
	private SamlAAMetadataService aaService;
	
	@Inject 
	private SamlSpConfigurationService spService;

	private String spEntityId = "https://bwidm-test.scc.kit.edu/sp";
	private String idpEntityId = "https://bwidm.scc.kit.edu/attribute-authority";
	private String persistentId = "ls1947@kit.edu";
	
	private Map<String, List<Object>> attributeMap;
	
	private String assertionString;
	
	public void query() {
			
		logger.debug("Making an attribute query for user {} {}", persistentId, idpEntityId);
		try {
			SamlSpConfigurationEntity spEntity = spService.findByEntityId(spEntityId);
			SamlMetadataEntity idpEntity = idpService.findByEntityId(idpEntityId);
			if (idpEntity == null)
				idpEntity = aaService.findByEntityId(idpEntityId);
			
			EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(
					idpEntity.getEntityDescriptor(), EntityDescriptor.class);
			
			Response samlResponse = attrQueryHelper.query(persistentId, idpEntity, idpEntityDescriptor, spEntity);
			
			Assertion assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor, spEntity, false);

			attributeMap = saml2AssertionService.extractAttributes(assertion);
			assertionString = samlHelper.prettyPrint(assertion);
			
		} catch (Exception e) {
			logger.warn("AttributeQuery failed", e);
			assertionString = "AttributeQuery failed" + e;
		}
	}

	public Map<String, List<Object>> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map<String, List<Object>> attributeMap) {
		this.attributeMap = attributeMap;
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

	public String getPersistentId() {
		return persistentId;
	}

	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	public String getAssertionString() {
		return assertionString;
	}
	
}
