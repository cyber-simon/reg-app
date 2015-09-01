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
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.reg.PasswordUtil;

@ManagedBean
@ViewScoped
public class ShowAdminUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private AdminUserService adminUserService;

	@Inject
	private RoleService roleService;
	
	@Inject
	private PasswordUtil passwordUtil;
	
	private AdminUserEntity entity;

	private DualListModel<RoleEntity> roleList;
	
	private Long id;
	
	private String newPassword;
	private Boolean hashPassword;
	private String[] hashMethod;
	private String selectedHashMethod;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = adminUserService.findByIdWithAttrs(id, "roles");
			roleList = new DualListModel<RoleEntity>();

			List<RoleEntity> targetList = new ArrayList<RoleEntity>(entity.getRoles());
			List<RoleEntity> sourceList = roleService.findAll();

			sourceList.removeAll(targetList);

			roleList.setSource(sourceList);
			roleList.setTarget(targetList);
		
			fillHashMethod();
			selectedHashMethod = hashMethod[0];
		}
	}

	protected void fillHashMethod() {
		Provider provider = Security.getProvider("BC");
		
		if (provider == null) {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			provider = Security.getProvider("BC");
		}
		
		List<String> algoList = new ArrayList<String>();
		
		for (Service service : provider.getServices()) {
			if (service.getType().equals("MessageDigest")) {
				algoList.add(service.getAlgorithm());
			}
		}
		
		hashMethod = algoList.toArray(new String[]{});
	}
	
	public void onTransfer(TransferEvent event) {

		if (event.isAdd()) {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				entity.getRoles().add(role);
				entity = adminUserService.save(entity);
			}
		}
		else {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				entity.getRoles().remove(role);
				entity = adminUserService.save(entity);
			}
		}
		entity = adminUserService.findByIdWithAttrs(id, "roles");
	}

	public void savePassword() {
		if (newPassword != null) {
			newPassword = newPassword.trim();
			
			if (hashPassword) {
				try {
					String hash = passwordUtil.generatePassword(selectedHashMethod, newPassword);
					entity.setPassword(hash);
				} catch (NoSuchAlgorithmException e) {
					logger.warn("Oh no", e);
				} catch (UnsupportedEncodingException e) {
					logger.warn("Oh no", e);
				}
			}
			else {
				entity.setPassword(newPassword);
			}
			entity = adminUserService.save(entity);
			entity = adminUserService.findByIdWithAttrs(id, "roles");
			
			newPassword = "";
		}
	}
	
	public void handleSave() {
		entity = adminUserService.save(entity);
		entity = adminUserService.findByIdWithAttrs(id, "roles");
	}
	
	public AdminUserEntity getEntity() {
		return entity;
	}

	public void setEntity(AdminUserEntity entity) {
		this.entity = entity;
	}

	public DualListModel<RoleEntity> getRoleList() {
		return roleList;
	}

	public void setRoleList(DualListModel<RoleEntity> roleList) {
		this.roleList = roleList;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public Boolean getHashPassword() {
		return hashPassword;
	}

	public void setHashPassword(Boolean hashPassword) {
		this.hashPassword = hashPassword;
	}

	public String[] getHashMethod() {
		return hashMethod;
	}

	public void setHashMethod(String[] hashMethod) {
		this.hashMethod = hashMethod;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSelectedHashMethod() {
		return selectedHashMethod;
	}

	public void setSelectedHashMethod(String selectedHashMethod) {
		this.selectedHashMethod = selectedHashMethod;
	}	
}
