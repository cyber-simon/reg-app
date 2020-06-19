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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.SamlAssertionEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ASUserAttrService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.SamlAssertionService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class ServiceAdminUserDetailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private FacesMessageGenerator messageGenerator;

    @Inject
    private RegistryService service;

    @Inject
    private UserService userService;
    
    @Inject
    private AuthorizationBean authBean;

    @Inject
    private RegisterUserService registerUserService;

	@Inject
	private GroupService groupService;
	
	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject
	private ASUserAttrService asUserAttrService;
	
	@Inject
	private SamlAssertionService samlAssertionService;

    @Inject
    private SessionManager sessionManager;
    
    private RegistryEntity entity;
    
    private UserEntity user;
    
	private List<GroupEntity> groupList;
	
	private List<ASUserAttrEntity> asUserAttrList;

	private SamlAssertionEntity samlAssertion;
	
    private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAgreements(id);			
		}
		
		if (! (authBean.isUserServiceAdmin(entity.getService()) || 
				authBean.isUserServiceHotline(entity.getService())))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public void reconsiliation() {
		logger.debug("Manual quick recon for Account {} Service {}", entity.getUser().getEppn(), entity.getService().getName());
		try {
			registerUserService.reconsiliation(entity, false, "service-admin-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.error("An error occured", e);
		}
	}
	
	public void fullReconsiliation() {
		logger.debug("Manual full recon for Account {} Service {}", entity.getUser().getEppn(), entity.getService().getName());
		try {
			registerUserService.reconsiliation(entity, true, "service-admin-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.error("An error occured", e);
		}
	}

	public void checkRegistry() {
		logger.info("Trying to check registry {} for user {}", entity.getId(), getUser().getEppn());
		
		List<RegistryEntity> tempRegistryList = new ArrayList<RegistryEntity>();
		tempRegistryList.add(entity);
		knowledgeSessionService.checkRules(tempRegistryList, user, "user-" + sessionManager.getUserId(), false);
	}
	
	public void deregister() {
		try {
			logger.info("Deregister registry {} via AdminRegistry page", entity.getId());
			registerUserService.deregisterUser(entity, "service-admin-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Could not deregister User", e);
		}
	}

	public void updateFromIdp() {
		logger.info("Trying user update for {}", getUser().getEppn());

		if (getUser() instanceof SamlUserEntity) {
			try {
				user = userService.updateUserFromIdp((SamlUserEntity) getUser(), "user-" + sessionManager.getUserId());
				messageGenerator.addInfoMessage("Info", "SAML AttributeQuery went through without errors");
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
				messageGenerator.addErrorMessage("Problem", "Exception while Querying IDP: " + e.getMessage() + extendedInfo);
			}
		}
		else {
			logger.info("No update method available for class {}", getUser().getClass().getName());
			messageGenerator.addErrorMessage("Problem", "No update method available for class " + getUser().getClass().getName());
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RegistryEntity getEntity() {
		return entity;
	}

	public void setEntity(RegistryEntity entity) {
		this.entity = entity;
	}

	public UserEntity getUser() {
		if (user == null) {
			user = userService.findByIdWithAttrs(entity.getUser().getId(), "genericStore", "attributeStore");			
		}
		return user;
	}

	public List<GroupEntity> getGroupList() {
		if (groupList == null)
			groupList = groupService.findByUser(getUser());
		return groupList;
	}

	public List<ASUserAttrEntity> getAsUserAttrList() {
		if (asUserAttrList == null)
			asUserAttrList = asUserAttrService.findForUserWithValues(getUser());
		return asUserAttrList;
	}

	public SamlAssertionEntity getSamlAssertion() {
		if (samlAssertion == null) {
			samlAssertion = samlAssertionService.findByUserId(getUser().getId());
		}
		return samlAssertion;
	}
}
