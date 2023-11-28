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
package edu.kit.scc.webreg.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserCreateService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class RegisterUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject 
    private SessionManager sessionManager;
	
    @Inject
    private UserService service;

    @Inject
    private UserCreateService userCreateService;

	@Inject 
	private SamlIdpMetadataService idpService;
	
	@Inject 
	private SamlSpConfigurationService spService;

	@Inject
	private AttributeMapHelper attrHelper;
		
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private SamlUserEntity entity;
	
	private Boolean errorState = false;
	private Boolean eppnError = false;
	private Boolean eppnOverride = false;
	
	private Map<String, String> printableAttributesMap;
	private Map<String, String> unprintableAttributesMap;
	private List<String> printableAttributesList;
	
    public void preRenderView(ComponentSystemEvent ev) {
    	if (entity == null) {
    		SamlIdpMetadataEntity idpEntity = idpService.fetch(sessionManager.getIdpId());
	    	SamlSpConfigurationEntity spConfigEntity = spService.fetch(sessionManager.getSpId());
	    	
	    	try {
	        	entity = userCreateService.preCreateUser(idpEntity, spConfigEntity, sessionManager.getSamlIdentifier(),
	        			sessionManager.getLocale(), sessionManager.getAttributeMap());
	        	
			} catch (UserUpdateException e) {
				errorState = true;
				messageGenerator.addResolvedErrorMessage("missing-mandatory-attributes", e.getMessage(), true);
				return;
			}
	    	
	    	if (service.findByEppn(entity.getEppn()).size() > 0) {
				eppnError = true;
	    	}
	    	
	    	printableAttributesMap = new HashMap<String, String>();
	    	unprintableAttributesMap = new HashMap<String, String>();
	    	printableAttributesList = new ArrayList<String>();
	    	
	    	for (Entry<String, List<Object>> entry : sessionManager.getAttributeMap().entrySet()) {
	    		if (entry.getKey().equals("urn:oid:0.9.2342.19200300.100.1.3")){
	    			printableAttributesList.add("email");
	    			printableAttributesMap.put("email", attrHelper.attributeListToString(entry.getValue(), ","));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")){
	    			printableAttributesList.add("eppn");
	    			printableAttributesMap.put("eppn", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("urn:oid:2.5.4.42")){
	    			printableAttributesList.add("given_name");
	    			printableAttributesMap.put("given_name", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("urn:oid:2.5.4.4")){
	    			printableAttributesList.add("sur_name");
	    			printableAttributesMap.put("sur_name", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.1.1.1.1")){
	    			printableAttributesList.add("gid_number");
	    			printableAttributesMap.put("gid_number", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("http://bwidm.de/bwidmCC")){
	    			printableAttributesList.add("primary_group");
	    			printableAttributesMap.put("primary_group", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("http://bwidm.de/bwidmOrgId")){
	    			printableAttributesList.add("bwidm_org_id");
	    			printableAttributesMap.put("bwidm_org_id", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("memberOf")){
	    			printableAttributesList.add("groups");
	    			printableAttributesMap.put("groups", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("http://bwidm.de/bwidmMemberOf")){
	    			printableAttributesList.add("groups");
	    			printableAttributesMap.put("groups", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.7")){
	    			printableAttributesList.add("entitlement");
	    			printableAttributesMap.put("entitlement", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:0.9.2342.19200300.100.1.1")){
	    			printableAttributesList.add("uid");
	    			printableAttributesMap.put("uid", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.1.1.1.0")){
	    			printableAttributesList.add("uid_number");
	    			printableAttributesMap.put("uid_number", attrHelper.getSingleStringFirst(entry.getValue()));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.9")){
	    			printableAttributesList.add("affiliation");
	    			printableAttributesMap.put("affiliation", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.13")){
	    			printableAttributesList.add("epuid");
	    			printableAttributesMap.put("epuid", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oasis:names:tc:SAML:attribute:pairwise-id")){
	    			printableAttributesList.add("pairwise_id");
	    			printableAttributesMap.put("pairwise_id", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oasis:names:tc:SAML:attribute:subject-id")){
	    			printableAttributesList.add("subject_id");
	    			printableAttributesMap.put("subject_id", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.1")){
	    			printableAttributesList.add("bwcard_number");
	    			printableAttributesMap.put("bwcard_number", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.2")){
	    			printableAttributesList.add("bwcard_chip_id");
	    			printableAttributesMap.put("bwcard_chip_id", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.3")){
	    			printableAttributesList.add("bwcard_escn");
	    			printableAttributesMap.put("bwcard_escn", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.4")){
	    			printableAttributesList.add("bwcard_valid_to");
	    			printableAttributesMap.put("bwcard_valid_to", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.149")){
	    			printableAttributesList.add("attributes.bundid.unknown");
	    			printableAttributesMap.put("attributes.bundid.unknown", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.261.94")){
	    			printableAttributesList.add("attributes.bundid.assurance");
	    			printableAttributesMap.put("attributes.bundid.assurance", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:2.5.4.16")){
	    			printableAttributesList.add("attributes.postal_address");
	    			printableAttributesMap.put("attributes.postal_address", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:2.5.4.17")){
	    			printableAttributesList.add("attributes.postal_code");
	    			printableAttributesMap.put("attributes.postal_code", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:2.5.4.18")){
	    			printableAttributesList.add("attributes.bundid.postal_handle");
	    			printableAttributesMap.put("attributes.bundid.postal_handle", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.55")){
	    			printableAttributesList.add("attributes.birthdate");
	    			printableAttributesMap.put("attributes.birthdate", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.5.5.7.9.2")){
	    			printableAttributesList.add("attributes.place_of_birth");
	    			printableAttributesMap.put("attributes.place_of_birth", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:0.9.2342.19200300.100.1.40")){
	    			printableAttributesList.add("attributes.personal_title");
	    			printableAttributesMap.put("attributes.personal_title", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.225599")){
	    			printableAttributesList.add("attributes.bundid.nationality");
	    			printableAttributesMap.put("attributes.bundid.nationality", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.225566")){
	    			printableAttributesList.add("attributes.birth_name");
	    			printableAttributesMap.put("attributes.birth_name", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25484.494450.3")){
	    			printableAttributesList.add("attributes.bundid.bpk2");
	    			printableAttributesMap.put("attributes.bundid.bpk2", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25484.494450.2")){
	    			printableAttributesList.add("attributes.bundid.assertion_proved_by");
	    			printableAttributesMap.put("attributes.bundid.assertion_proved_by", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25484.494450.1")){
	    			printableAttributesList.add("attributes.bundid.assertion_valid_until");
	    			printableAttributesMap.put("attributes.bundid.assertion_valid_until", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.33592.1.3.5")){
	    			printableAttributesList.add("attributes.bundid.gender");
	    			printableAttributesMap.put("attributes.bundid.gender", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else if (entry.getKey().equals("urn:oid:2.5.4.7")){
	    			printableAttributesList.add("attributes.locality_name");
	    			printableAttributesMap.put("attributes.locality_name", attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    		else {
	    			unprintableAttributesMap.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue(), ", "));
	    		}
	    	}
    	}
	}

    public String save() {

		if (errorState) {
			/*
			 * There are unresolved errors. Cannot persist user.
			 */
			return null;
		}
		else if (eppnError && (! eppnOverride)) {
			/*
			 * EPPN is already in system, but not aknowledged
			 */
			return null;
		}

		try {
			entity = userCreateService.createUser(entity, sessionManager.getAttributeMap(), null, null);
		} catch (UserUpdateException e) {
			logger.warn("An error occured whilst creating user", e);
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			return null;
		}

    	sessionManager.setIdentityId(entity.getIdentity().getId());
    	
		if (sessionManager.getOriginalRequestPath() != null) {
			String orig = sessionManager.getOriginalRequestPath();
			sessionManager.setOriginalRequestPath(null);
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect(orig);
			} catch (IOException e) {
				messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			}
			return null;
		}
		else
			return "/index.xhtml?faces-redirect=true";
    }
    
	public UserEntity getEntity() {
		return entity;
	}

	public void setEntity(SamlUserEntity entity) {
		this.entity = entity;
	}

	public SamlIdpMetadataEntity getIdpEntity() {
		return idpService.fetch(sessionManager.getIdpId());
	}

	public Boolean getErrorState() {
		return errorState;
	}

	public void setErrorState(Boolean errorState) {
		this.errorState = errorState;
	}

	public Map<String, String> getPrintableAttributesMap() {
		return printableAttributesMap;
	}

	public Map<String, String> getUnprintableAttributesMap() {
		return unprintableAttributesMap;
	}

	public List<String> getPrintableAttributesList() {
		return printableAttributesList;
	}

	public Boolean getEppnOverride() {
		return eppnOverride;
	}

	public void setEppnOverride(Boolean eppnOverride) {
		this.eppnOverride = eppnOverride;
	}

	public Boolean getEppnError() {
		return eppnError;
	}

	public void setEppnError(Boolean eppnError) {
		this.eppnError = eppnError;
	}

	public List<UserEntity> getOldUserList() {
		return service.findByEppn(entity.getEppn());
	}

	
}
