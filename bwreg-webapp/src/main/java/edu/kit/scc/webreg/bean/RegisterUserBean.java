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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserCreateService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
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
	
	private UserEntity entity;
	private SamlIdpMetadataEntity idpEntity;
	private SamlSpConfigurationEntity spConfigEntity;
	
	private Boolean errorState = false;
	
	private Map<String, String> printableAttributesMap;
	private Map<String, String> unprintableAttributesMap;
	private List<String> printableAttributesList;
	
    public void preRenderView(ComponentSystemEvent ev) {
    	idpEntity = idpService.findById(sessionManager.getIdpId());
    	spConfigEntity = spService.findById(sessionManager.getSpId());
    	
    	try {
        	entity = userCreateService.preCreateUser(idpEntity, spConfigEntity, sessionManager.getPersistentId(),
        			sessionManager.getLocale(), sessionManager.getAttributeMap());
        	
		} catch (UserUpdateException e) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("missing-mandatory-attributes", e.getMessage(), true);
			return;
		}
    	
    	if (service.findByEppn(entity.getEppn()) != null) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("eppn-blocked", "eppn-blocked-detail", true);
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
    		else {
    			unprintableAttributesMap.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue(), ", "));
    		}
    	}
	}

    public String save() {

		try {
			entity = userCreateService.createUser(entity, sessionManager.getAttributeMap(), null);
		} catch (UserUpdateException e) {
			logger.warn("An error occured whilst creating user", e);
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			return null;
		}

    	sessionManager.setUserId(entity.getId());
    	
		if (sessionManager.getOriginalRequestPath() != null) {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect(sessionManager.getOriginalRequestPath());
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

	public void setEntity(UserEntity entity) {
		this.entity = entity;
	}

	public SamlIdpMetadataEntity getIdpEntity() {
		return idpEntity;
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

	
}
