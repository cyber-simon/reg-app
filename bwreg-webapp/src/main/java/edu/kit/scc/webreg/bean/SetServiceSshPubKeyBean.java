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

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryStatus;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.SshPubKeyUsageType;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.ssh.SshPubKeyRegistryService;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class SetServiceSshPubKeyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String[] usageTypes = { "Interactive", "Command" };
	
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
    private SshPubKeyService sshPubKeyService;

    @Inject
    private SshPubKeyRegistryService sshPubKeyRegistryService;
    
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private RegistryEntity registryEntity;
	private ServiceEntity serviceEntity;
	private UserEntity userEntity;
	private IdentityEntity identity;
	
	private Long id;
	private String serviceShortName;
	
	private List<SshPubKeyRegistryEntity> sshPubKeyRegistryList;
	private List<SshPubKeyEntity> sshPubKeyList;
	
	private SshPubKeyEntity selectedKey;
	private String selectedUsageType;
	private String newCommand, newFrom, newComment;
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			identity = identityService.fetch(sessionManager.getIdentityId());

			if (id != null) {
				registryEntity = registryService.fetch(id);

				if (registryEntity == null)
					throw new IllegalArgumentException("Service Registry not found");

				serviceEntity = registryEntity.getService();
			}
			else if (serviceShortName != null) {
				serviceEntity = serviceService.findByShortName(serviceShortName);
				
				if (serviceEntity == null)
					throw new IllegalArgumentException("Service not found");
				
				registryEntity = registryService.findByServiceAndUserAndStatus(serviceEntity, userEntity, RegistryStatus.ACTIVE);
			}

			userEntity = registryEntity.getUser();
			
			if (! registryEntity.getIdentity().getId().equals(identity.getId()))
				throw new NotAuthorizedException("Not authorized to view this item");

			if (! authBean.isUserInService(serviceEntity)) 
				throw new IllegalArgumentException("Not authorized for this service");

			sshPubKeyList = sshPubKeyService.findByIdentityAndStatus(identity.getId(), SshPubKeyStatus.ACTIVE);
			sshPubKeyRegistryList = sshPubKeyRegistryService.findByRegistry(registryEntity.getId());

			for (SshPubKeyRegistryEntity s : sshPubKeyRegistryList) {
				sshPubKeyList.remove(s.getSshPubKey());
			}
			
			initialized = true;
		}
	}
	
	public String save() {
		if (! (RegistryStatus.ACTIVE.equals(registryEntity.getRegistryStatus()) || 
				RegistryStatus.LOST_ACCESS.equals(registryEntity.getRegistryStatus()))) {
			messageGenerator.addResolvedErrorMessage("key_error", "error", "ssh_pub_key_cannot_be_set", true);
			return null;
		}
		
		SshPubKeyRegistryEntity sshPubKeyRegistry = sshPubKeyRegistryService.createNew();
		sshPubKeyRegistry.setRegistry(registryEntity);
		sshPubKeyRegistry.setSshPubKey(selectedKey);
		sshPubKeyRegistry.setComment(newComment);
		if ("interactive".equalsIgnoreCase(selectedUsageType)) {
			sshPubKeyRegistry.setUsageType(SshPubKeyUsageType.INTERACTIVE);
			sshPubKeyRegistry.setKeyStatus(SshPubKeyRegistryStatus.ACTIVE);
		}
		else if ("command".equalsIgnoreCase(selectedUsageType)) {
			sshPubKeyRegistry.setUsageType(SshPubKeyUsageType.COMMAND);
			sshPubKeyRegistry.setCommand(newCommand);
			sshPubKeyRegistry.setFrom(newFrom);
			sshPubKeyRegistry.setKeyStatus(SshPubKeyRegistryStatus.PENDING);
		}
		else {
			messageGenerator.addResolvedErrorMessage("key_error", "error", "ssh_pub_key_cannot_be_set", true);
			return null;
		}
		
		sshPubKeyRegistry = sshPubKeyRegistryService.deployRegistry(sshPubKeyRegistry, "user-" + userEntity.getId());
		sshPubKeyRegistryList.add(sshPubKeyRegistry);
		sshPubKeyList.remove(selectedKey);
		
		return null;
	}

	public String delete(SshPubKeyRegistryEntity reg) {
		sshPubKeyRegistryService.deleteRegistry(reg, "user-" + userEntity.getId());
		sshPubKeyRegistryList.remove(reg);
		sshPubKeyList.add(reg.getSshPubKey());
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

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}

	public List<SshPubKeyRegistryEntity> getSshPubKeyRegistryList() {
		return sshPubKeyRegistryList;
	}

	public void setSshPubKeyRegistryList(List<SshPubKeyRegistryEntity> sshPubKeyRegistryList) {
		this.sshPubKeyRegistryList = sshPubKeyRegistryList;
	}

	public List<SshPubKeyEntity> getSshPubKeyList() {
		return sshPubKeyList;
	}

	public void setSshPubKeyList(List<SshPubKeyEntity> sshPubKeyList) {
		this.sshPubKeyList = sshPubKeyList;
	}

	public String[] getUsageTypes() {
		return usageTypes;
	}

	public String getSelectedUsageType() {
		return selectedUsageType;
	}

	public void setSelectedUsageType(String selectedUsageType) {
		this.selectedUsageType = selectedUsageType;
	}

	public SshPubKeyEntity getSelectedKey() {
		return selectedKey;
	}

	public void setSelectedKey(SshPubKeyEntity selectedKey) {
		this.selectedKey = selectedKey;
	}

	public String getNewCommand() {
		return newCommand;
	}

	public void setNewCommand(String newCommand) {
		this.newCommand = newCommand;
	}

	public String getNewFrom() {
		return newFrom;
	}

	public void setNewFrom(String newFrom) {
		this.newFrom = newFrom;
	}

	public String getNewComment() {
		return newComment;
	}

	public void setNewComment(String newComment) {
		this.newComment = newComment;
	}
}
