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

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.SessionManager;

@ManagedBean
@ViewScoped
public class SetServicePasswordBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private RegistryService registryService;

	@Inject
	private AuthorizationBean authBean;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private UserService userService;
	
    @Inject
    private RegisterUserService registerUserService;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private RegistryEntity registryEntity;
	private ServiceEntity serviceEntity;
	private UserEntity userEntity;
	
	private Long id;
	
	private String password1, password2;
	
	private Long initializedId;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (id != initializedId) {
			registryEntity = registryService.findById(id);
			if (registryEntity == null)
				throw new IllegalArgumentException("Service Registry not found");
			
			userEntity = userService.findById(sessionManager.getUserId());
			serviceEntity = registryEntity.getService();
	
			if (! authBean.isUserInService(serviceEntity)) 
				throw new IllegalArgumentException("Not authorized for this service");

			password1 = null;
			password2 = null;
			
			initializedId = id;
		}
	}

	public String save() {
		if (! (RegistryStatus.ACTIVE.equals(registryEntity.getRegistryStatus()) || 
				RegistryStatus.LOST_ACCESS.equals(registryEntity.getRegistryStatus()))) {
			FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fehler:", "Servicepasswort kann für diesen Dienst nicht gesetzt werden"));			
			return null;
		}
		
		if (password1 == null || password2 == null ||
				"".equals(password1) || "".equals(password2)) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_empty", "password_field_empty_detail", true);
			return null;
		}
		else if (! password1.equals(password2)) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_different", "password_field_different_detail", true);
			return null;
		}

		RegisterUserWorkflow registerUserWorkflow = registerUserService.getWorkflowInstance(serviceEntity.getRegisterBean());
		
		if (registerUserWorkflow instanceof SetPasswordCapable) {
			try {
				registerUserService.setPassword(userEntity, serviceEntity, registryEntity, password1, "user-self");
				password1 = null;
				password2 = null;
				FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_INFO, "Passwort geändert:", "Das Passwort wurde bei dem Dienst geändert"));
			} catch (RegisterException e) {
				FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Konnte das Passwort nicht setzen:", e.getMessage()));
			}
		}
		else
			FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fehler:", "Servicepasswort kann für diesen Dienst nicht gesetzt werden"));

		return null;
	}
	
	public String deleteServicePassword() {
		if (! (RegistryStatus.ACTIVE.equals(registryEntity.getRegistryStatus()) || 
				RegistryStatus.LOST_ACCESS.equals(registryEntity.getRegistryStatus()))) {
			FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fehler:", "Servicepasswort kann für diesen Dienst nicht gelöscht werden"));			
			return null;
		}

		RegisterUserWorkflow registerUserWorkflow = registerUserService.getWorkflowInstance(serviceEntity.getRegisterBean());
		if (registerUserWorkflow instanceof SetPasswordCapable) {
			try {
				registerUserService.deletePassword(userEntity, serviceEntity, registryEntity, "user-self");
			} catch (RegisterException e) {
				FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Konnte das Passwort nicht löschen:", e.getMessage()));
			}
		}
		else
			FacesContext.getCurrentInstance().addMessage("pw_error", new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fehler:", "Servicepasswort kann für diesen Dienst nicht geslöscht werden"));
		
		return null;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public String getPassword1() {
		return password1;
	}

	public void setPassword1(String password1) {
		this.password1 = password1;
	}

	public String getPassword2() {
		return password2;
	}

	public void setPassword2(String password2) {
		this.password2 = password2;
	}
}
