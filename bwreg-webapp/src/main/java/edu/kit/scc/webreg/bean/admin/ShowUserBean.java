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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.opensaml.saml.saml2.core.Attribute;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.AuditUserEntryService;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity_;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity_;
import edu.kit.scc.webreg.entity.audit.AuditUserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.ASUserAttrService;
import edu.kit.scc.webreg.service.AttributeSourceService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.service.oidc.client.OidcUserService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class ShowUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserService userService;

	@Inject
	private OidcUserService oidcUserService;

	@Inject
	private RoleService roleService;

	@Inject
	private RegistryService registryService;

	@Inject
	private GroupService groupService;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;

	@Inject
	private ASUserAttrService asUserAttrService;

	@Inject
	private AttributeSourceService attributeSourceService;

	@Inject
	private AuditUserEntryService auditUserEntryService;

	@Inject
	private SessionManager sessionManager;

	private UserEntity user;

	private DualListModel<RoleEntity> roleList;

	private Map<String, Attribute> attributeMap;

	private List<RegistryEntity> registryList;

	private List<GroupEntity> groupList;

	private List<ASUserAttrEntity> asUserAttrList;
	private AttributeSourceEntity selectedAttributeSource;
	private ASUserAttrEntity selectedUserAttr;
	private LazyDataModel<AuditUserEntity> auditUserEntryList;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
			user = userService.findByIdWithAttrs(id, UserEntity_.genericStore, UserEntity_.attributeStore);
		}
	}

	public void onTransfer(TransferEvent event) {
		user = userService.findByIdWithAll(user.getId());
		if (event.isAdd()) {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				roleService.addUserToRole(user, role.getName());
			}
		} else {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				roleService.removeUserFromRole(user, role.getName());
			}
		}
		user = userService.findByIdWithAll(user.getId());
	}

	public void updateFromIdp() {
		user = userService.findByIdWithAll(user.getId());
		logger.info("Trying user update for {}", user.getEppn());

		if (user instanceof SamlUserEntity) {
			try {
				userService.updateUserFromIdp((SamlUserEntity) user, "identity-" + sessionManager.getIdentityId());
			} catch (UserUpdateException e) {
				logger.info("Exception while Querying IDP: {}", e.getMessage());
				if (e.getCause() != null) {
					logger.info("Cause is: {}", e.getCause().getMessage());
					if (e.getCause().getCause() != null) {
						logger.info("Inner Cause is: {}", e.getCause().getCause().getMessage());
					}
				}
			}
		} else {
			logger.info("No update method available for class {}", user.getClass().getName());
		}
	}

	public void updateFromOp() {
		user = userService.findByIdWithAll(user.getId());
		logger.info("Trying user update for {}", user.getEppn());

		if (user instanceof OidcUserEntity) {
			try {
				oidcUserService.updateUserFromOp((OidcUserEntity) user, "identity-" + sessionManager.getIdentityId());
			} catch (UserUpdateException e) {
				logger.info("Exception while Querying IDP: {}", e.getMessage());
				if (e.getCause() != null) {
					logger.info("Cause is: {}", e.getCause().getMessage());
					if (e.getCause().getCause() != null) {
						logger.info("Inner Cause is: {}", e.getCause().getCause().getMessage());
					}
				}
			}
		} else {
			logger.info("No update method available for class {}", user.getClass().getName());
		}
	}

	public void checkAllRegistries() {
		user = userService.findByIdWithAll(user.getId());
		logger.info("Trying to check all registries for user {}", user.getEppn());

		List<RegistryEntity> tempRegistryList = new ArrayList<RegistryEntity>();
		for (RegistryEntity registry : registryList) {
			if (RegistryStatus.ACTIVE.equals(registry.getRegistryStatus())
					|| RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())) {
				tempRegistryList.add(registry);
			}
		}
		for (RegistryEntity registry : tempRegistryList) {
			knowledgeSessionService.checkServiceAccessRule(registry.getUser(), registry.getService(), registry, 
					"identity-" + sessionManager.getIdentityId(), false);
		}
	}

	public UserEntity getEntity() {
		return user;
	}

	public void setEntity(UserEntity user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Attribute> getAttributeValues() {
		if (attributeMap != null)
			return new ArrayList<Attribute>(attributeMap.values());
		else
			return null;
	}

	public Map<String, Attribute> getAttributeMap() {
		return attributeMap;
	}

	public DualListModel<RoleEntity> getRoleList() {
		if (roleList == null) {
			roleList = new DualListModel<RoleEntity>();
			List<RoleEntity> targetList = roleService.findByUser(user);
			roleList.setTarget(targetList);
			List<RoleEntity> sourceList = roleService.findAll();
			sourceList.removeAll(targetList);
			roleList.setSource(sourceList);
		}
		return roleList;
	}

	public void setRoleList(DualListModel<RoleEntity> roleList) {
		this.roleList = roleList;
	}

	public List<RegistryEntity> getRegistryList() {
		if (registryList == null)
			registryList = registryService.findByUser(user);
		return registryList;
	}

	public List<GroupEntity> getGroupList() {
		if (groupList == null)
			groupList = groupService.findByUser(user);
		return groupList;
	}

	public List<ASUserAttrEntity> getAsUserAttrList() {
		if (asUserAttrList == null)
			asUserAttrList = asUserAttrService.findForUser(user);
		return asUserAttrList;
	}

	public ASUserAttrEntity getSelectedUserAttr() {
		return selectedUserAttr;
	}

	public void setSelectedUserAttr(ASUserAttrEntity selectedUserAttr) {
		selectedUserAttr = asUserAttrService.findByIdWithAttrs(selectedUserAttr.getId(), ASUserAttrEntity_.values);
		selectedAttributeSource = attributeSourceService.findByIdWithAttrs(
				selectedUserAttr.getAttributeSource().getId(), AttributeSourceEntity_.attributeSourceServices);
		this.selectedUserAttr = selectedUserAttr;
	}

	public AttributeSourceEntity getSelectedAttributeSource() {
		return selectedAttributeSource;
	}

	public LazyDataModel<AuditUserEntity> getAuditUserEntryList() {
		if (auditUserEntryList == null) {
			auditUserEntryList = new GenericLazyDataModelImpl<AuditUserEntity, AuditUserEntryService>(
					auditUserEntryService, equal("user", user));
		}
		return auditUserEntryList;
	}
}
