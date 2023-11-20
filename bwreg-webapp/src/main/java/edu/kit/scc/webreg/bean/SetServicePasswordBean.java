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

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.tools.PropertyReader;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class SetServicePasswordBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private RegistryService registryService;

	@Inject
	private ServiceService serviceService;

	@Inject
	private AuthorizationBean authBean;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private IdentityService identityService;

	@Inject
	private RegisterUserService registerUserService;

	@Inject
	private FacesMessageGenerator messageGenerator;

	private RegistryEntity registryEntity;
	private ServiceEntity serviceEntity;
	private UserEntity userEntity;
	private IdentityEntity identity;

	private Long id;
	private String serviceShortName;
	private String navOptions;
	private String redirectTarget;

	private Boolean hideDeletePasswordBtn = false;

	private String password1, password2;

	private Boolean initialized = false;

	public void preRenderView(ComponentSystemEvent ev) {
		if (!initialized) {
			identity = identityService.fetch(sessionManager.getIdentityId());

			if (id != null) {
				registryEntity = registryService.fetch(id);

				if (registryEntity == null)
					throw new IllegalArgumentException("Service Registry not found");

				serviceEntity = registryEntity.getService();
			} else if (serviceShortName != null) {
				serviceEntity = serviceService.findByShortName(serviceShortName);

				if (serviceEntity == null)
					throw new IllegalArgumentException("Service not found");

				registryEntity = registryService.findByServiceAndUserAndStatus(serviceEntity, userEntity,
						RegistryStatus.ACTIVE);
			}

			userEntity = registryEntity.getUser();

			if (!registryEntity.getIdentity().getId().equals(identity.getId()))
				throw new NotAuthorizedException("Not authorized to view this item");

			if (!authBean.isUserInService(serviceEntity))
				throw new IllegalArgumentException("Not authorized for this service");

			serviceEntity = serviceService.findByIdWithServiceProps(serviceEntity.getId());
			try {
				PropertyReader prop = PropertyReader.newRegisterPropReader(serviceEntity);
				if ("true".equalsIgnoreCase(prop.readPropOrNull("hide_delete_password_btn"))) {
					hideDeletePasswordBtn = true;
				}
			} catch (RegisterException e) {
				throw new IllegalArgumentException("service is not configured yet");
			}

			password1 = null;
			password2 = null;

			if (navOptions != null && navOptions.equalsIgnoreCase("created")) {
				redirectTarget = "../service/registry-detail.xhtml?regId=" + registryEntity.getId();
			}

			initialized = true;
		}
	}

	public String save() {
		if (!(RegistryStatus.ACTIVE.equals(registryEntity.getRegistryStatus())
				|| RegistryStatus.LOST_ACCESS.equals(registryEntity.getRegistryStatus()))) {
			messageGenerator.addResolvedErrorMessage("pw_error", "error", "service_password_cannot_be_set", true);
			return null;
		}

		if (password1 == null || password2 == null || "".equals(password1) || "".equals(password2)) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_empty",
					"password_field_empty_detail", true);
			return null;
		} else if (!password1.equals(password2)) {
			messageGenerator.addResolvedWarningMessage("pw_error", "password_field_different",
					"password_field_different_detail", true);
			return null;
		}

		RegisterUserWorkflow registerUserWorkflow = registerUserService
				.getWorkflowInstance(serviceEntity.getRegisterBean());

		if (registerUserWorkflow instanceof SetPasswordCapable) {
			try {
				registerUserService.setPassword(userEntity, serviceEntity, registryEntity, password1, "user-self");

				if (redirectTarget != null) {
					ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
					try {
						context.redirect(redirectTarget);
					} catch (IOException e) {
					}
				}

				password1 = null;
				password2 = null;
				messageGenerator.addResolvedInfoMessage("pw_error", "service_password_changed",
						"service_password_changed_detail", true);
			} catch (RegisterException e) {
				messageGenerator.addResolvedErrorMessage("pw_error", "service_password_cannot_be_set", e.getMessage(),
						false);
			}
		} else {
			messageGenerator.addResolvedErrorMessage("pw_error", "error", "service_password_cannot_be_set", true);
		}

		return null;
	}

	public String deleteServicePassword() {
		if (hideDeletePasswordBtn) {
			// diasble the button functionality as it is hidden
			return null;
		}

		if (!(RegistryStatus.ACTIVE.equals(registryEntity.getRegistryStatus())
				|| RegistryStatus.LOST_ACCESS.equals(registryEntity.getRegistryStatus()))) {
			messageGenerator.addResolvedErrorMessage("pw_error", "error", "service_password_cannot_be_deleted", true);
			return null;
		}

		RegisterUserWorkflow registerUserWorkflow = registerUserService
				.getWorkflowInstance(serviceEntity.getRegisterBean());
		if (registerUserWorkflow instanceof SetPasswordCapable) {
			try {
				registerUserService.deletePassword(userEntity, serviceEntity, registryEntity, "user-self");
				messageGenerator.addResolvedInfoMessage("pw_error", "service_password_deleted",
						"service_password_deleted_detail", true);
			} catch (RegisterException e) {
				messageGenerator.addResolvedErrorMessage("pw_error", "service_password_cannot_be_deleted",
						e.getMessage(), false);
			}
		} else
			messageGenerator.addResolvedErrorMessage("pw_error", "error", "service_password_cannot_be_set", true);

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

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}

	public String getNavOptions() {
		return navOptions;
	}

	public void setNavOptions(String navOptions) {
		this.navOptions = navOptions;
	}

	public Boolean getHideDeletePasswordBtn() {
		return hideDeletePasswordBtn;
	}
}
