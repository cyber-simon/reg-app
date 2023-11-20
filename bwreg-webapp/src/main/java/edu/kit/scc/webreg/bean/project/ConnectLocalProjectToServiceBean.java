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
package edu.kit.scc.webreg.bean.project;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity_;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectPolicyType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceStatusType;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.PolicyService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.PolicyHolder;

@Named
@ViewScoped
public class ConnectLocalProjectToServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ConnectLocalProjectToServiceBean.class);

	@Inject
	private SessionManager session;

	@Inject
	private FacesMessageGenerator messageGenerator;

	@Inject
	private LocalProjectService service;

	@Inject
	private ServiceService serviceService;

	@Inject
	private ProjectService projectService;

	@Inject
	private PolicyService policyService;

	private LocalProjectEntity entity;

	private List<ServiceEntity> serviceList;
	private List<ServiceEntity> selectedServices;
	private List<ProjectServiceEntity> projectServiceList;

	private Long id;

	private List<ProjectIdentityAdminEntity> adminList;
	private ProjectIdentityAdminEntity adminIdentity;

	private List<PolicyHolder> policyHolderList;

	private Boolean savePossible = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		selectedServices = new ArrayList<ServiceEntity>();

		for (ProjectIdentityAdminEntity a : getAdminList()) {
			if (a.getIdentity().getId().equals(session.getIdentityId())) {
				adminIdentity = a;
				break;
			}
		}

		if (adminIdentity == null) {
			throw new NotAuthorizedException("Nicht autorisiert");
		} else {
			if (!(ProjectAdminType.ADMIN.equals(adminIdentity.getType())
					|| ProjectAdminType.OWNER.equals(adminIdentity.getType()))) {
				throw new NotAuthorizedException("Nicht autorisiert");
			}
		}
	}

	public void connectionAdded() {
		policyHolderList = new ArrayList<PolicyHolder>();
		if (selectedServices.size() > 0) {
			setSavePossible(true);
		}
		else {
			setSavePossible(false);
		}
		
		for (ServiceEntity s : selectedServices) {
			List<PolicyEntity> policyList = policyService.findAllByAttr("projectPolicy", s);

			policyList.stream().forEach(policy -> {
				if (ProjectPolicyType.SERVICE_CONNECT.equals(policy.getProjectPolicyType())) {
					PolicyHolder ph = new PolicyHolder();
					ph.setPolicy(policy);
					policyHolderList.add(ph);
				}
			});

		}
	}

	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(getEntity());
		}
		return adminList;
	}

	public String save() {
		logger.debug("testing all checkboxes");
		for (PolicyHolder ph : policyHolderList) {
			if (ph.getPolicy() != null && ph.getPolicy().getShowOnly() != null && ph.getPolicy().getShowOnly()) {
				logger.debug("Policy {} in Service {} is just for show", ph.getPolicy().getId(),
						ph.getPolicy().getProjectPolicy().getId());
			} else if (!ph.getChecked()) {
				logger.debug("Policy {} in Service {} is not checked", ph.getPolicy().getId(),
						ph.getPolicy().getProjectPolicy().getId());
				messageGenerator.addWarningMessage("need_check", "Zustimmung fehlt!",
						"Sie müssen allen Nutzungbedingungen zustimmen.");
				return "";
			} else {
				logger.debug("Policy {} in Service {} is checked", ph.getPolicy().getId(),
						ph.getPolicy().getProjectPolicy().getId());
			}
		}

		for (ServiceEntity s : selectedServices) {
			projectService.addOrChangeService(entity, s, ProjectServiceType.PASSIVE_GROUP,
					ProjectServiceStatusType.APPROVAL_PENDING, "idty-" + session.getIdentityId());
		}
		return "show-local-project.xhtml?faces-redirect=true&id=" + getEntity().getId();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, LocalProjectEntity_.projectServices);
		}

		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}

	public List<ServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = serviceService.findAllByAttr("projectCapable", Boolean.TRUE);
			for (ProjectServiceEntity pse : getProjectServiceList()) {
				serviceList.remove(pse.getService());
			}
		}
		return serviceList;
	}

	public List<ServiceEntity> getSelectedServices() {
		return selectedServices;
	}

	public void setSelectedServices(List<ServiceEntity> selectedServices) {
		this.selectedServices = selectedServices;
	}

	public List<ProjectServiceEntity> getProjectServiceList() {
		if (projectServiceList == null) {
			projectServiceList = projectService.findServicesForProject(entity);
		}
		return projectServiceList;
	}

	public void setProjectServiceList(List<ProjectServiceEntity> projectServiceList) {
		this.projectServiceList = projectServiceList;
	}

	public List<PolicyHolder> getPolicyHolderList() {
		if (policyHolderList == null) {
			policyHolderList = new ArrayList<PolicyHolder>();
		}
		return policyHolderList;
	}

	public Boolean getSavePossible() {
		return savePossible;
	}

	public void setSavePossible(Boolean savePossible) {
		this.savePossible = savePossible;
	}
}
