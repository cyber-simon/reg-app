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
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class CheckAccessBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	private IdentityEntity identity;
	private ServiceEntity service;
	private RegistryEntity registry;

	private Long id;

	private boolean initialized = false;

	private Boolean accessProblem = false;

	@Inject
	private FacesMessageGenerator messageGenerator;

	@Inject
	private RegistryService registryService;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private IdentityService identityService;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;

	public void preRenderView(ComponentSystemEvent ev) {
		if (!initialized) {
			identity = identityService.fetch(sessionManager.getIdentityId());
			registry = registryService.fetch(id);

			if (!registry.getIdentity().getId().equals(identity.getId())) {
				throw new NotAuthorizedException("no authorized to view this item");
			}

			service = registry.getService();
			user = registry.getUser();

			checkServiceAccess(registry.getService());
		}
	}

	private void checkServiceAccess(ServiceEntity service) {

		List<Object> objectList = knowledgeSessionService.checkServiceAccessRule(user, service, registry, "user-self",
				false);

		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				return;
			}
		}

		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				String s = ((UnauthorizedUser) o).getMessage();
				messageGenerator.addResolvedErrorMessage("reqs", "error", s, true);
				accessProblem = true;
			}
		}
	}

	public UserEntity getUser() {
		return user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ServiceEntity getService() {
		return service;
	}

	public RegistryEntity getRegistry() {
		return registry;
	}

	public Boolean getAccessProblem() {
		return accessProblem;
	}
}
