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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.session.SessionManager;

@ManagedBean
@ViewScoped
public class RegistryDetailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private RegistryService service;

	@Inject
	private ServiceService serviceService;
	
	@Inject
	private UserService userService;

    @Inject 
    private SessionManager sessionManager;

    @Inject
	private RegisterUserService registerUserService;
	
	private Boolean initialzed = false;
	
	private RegistryEntity entity;
	private ServiceEntity serviceEntity;
	private UserEntity userEntity;
	
	private Infotainment infotainment;
	private TreeNode infotainmentRoot;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
    	if (! initialzed) {
			entity = service.findById(id);
			
			if (entity == null)
				throw new NotAuthorizedException("No such item");
			
			serviceEntity = serviceService.findByIdWithServiceProps(entity.getService().getId());
			userEntity = userService.findById(sessionManager.getUserId());

			if (! entity.getUser().getId().equals(userEntity.getId()))
				throw new NotAuthorizedException("Not authorized to view this item");

			if (entity.getRegistryStatus() == RegistryStatus.ACTIVE) {
				RegisterUserWorkflow registerWorkflow = registerUserService.getWorkflowInstance(entity.getService().getRegisterBean());
				if (registerWorkflow instanceof InfotainmentCapable) {
					try {
						infotainment = ((InfotainmentCapable) registerWorkflow).getInfo(entity, userEntity, serviceEntity);
						
						if (infotainment.getRoot() != null) {
							infotainmentRoot = new DefaultTreeNode("root", null);
							fillChildren(infotainmentRoot, infotainment.getRoot());
						}
							
					} catch (RegisterException e) {
						logger.warn("Getting Infotainment failed: {}", e.toString());
					}
				}
			}
			initialzed = true;
		}
	}
	
	private void fillChildren(TreeNode node, InfotainmentTreeNode infoNode) {
		for (InfotainmentTreeNode subInfoNode : infoNode.getChildren()) {
			TreeNode subNode = new DefaultTreeNode(subInfoNode, node);
			fillChildren(subNode, subInfoNode);
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

	public Infotainment getInfotainment() {
		return infotainment;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public TreeNode getInfotainmentRoot() {
		return infotainmentRoot;
	}

	public void setInfotainmentRoot(TreeNode infotainmentRoot) {
		this.infotainmentRoot = infotainmentRoot;
	}
}
