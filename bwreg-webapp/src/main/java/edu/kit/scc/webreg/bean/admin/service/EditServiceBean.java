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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.AdminRoleService;
import edu.kit.scc.webreg.service.ApproverRoleService;
import edu.kit.scc.webreg.service.BusinessRulePackageService;
import edu.kit.scc.webreg.service.BusinessRuleService;
import edu.kit.scc.webreg.service.GroupAdminRoleService;
import edu.kit.scc.webreg.service.ImageService;
import edu.kit.scc.webreg.service.ServiceService;

@ManagedBean
@ViewScoped
public class EditServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceService service;
	
	@Inject
	private ApproverRoleService approverRoleService;
	
	@Inject
	private AdminRoleService adminRoleService;
	
	@Inject
	private GroupAdminRoleService groupAdminRoleService;
	
	@Inject
	private ImageService imageService;
	
	@Inject
	private BusinessRuleService ruleService;
	
	@Inject
	private BusinessRulePackageService rulePackageService;
	
	private ServiceEntity entity;

	private List<ServiceEntity> parentServiceList;
	private List<ApproverRoleEntity> approverRoleList;
	private List<AdminRoleEntity> adminRoleList;
	private List<AdminRoleEntity> hotlineRoleList;
	private List<GroupAdminRoleEntity> groupAdminRoleList;
	private List<ImageEntity> imageList;
	private List<BusinessRuleEntity> ruleList;
	private List<BusinessRulePackageEntity> rulePackageList;
	
	private ServiceEntity selectedParentService;
	private ApproverRoleEntity selectedApproverRole;
	private AdminRoleEntity selectedAdminRole;
	private AdminRoleEntity selectedHotlineRole;
	private GroupAdminRoleEntity selectedGroupAdminRole;
	private ImageEntity selectedImage;
	private BusinessRuleEntity selectedRule;
	private BusinessRulePackageEntity selectedRulePackage;
	private BusinessRulePackageEntity selectedMandatoryValuesRulePackage;
	
	private Map<String, String> propertyMap;
	
	private String newKey, newValue;
	
	private Long id;

	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			entity = service.findByIdWithServiceProps(id);
			approverRoleList = approverRoleService.findAll();
			adminRoleList = adminRoleService.findAll();
			hotlineRoleList = adminRoleService.findAll();
			groupAdminRoleList = groupAdminRoleService.findAll();
			imageList = imageService.findAll();
			ruleList = ruleService.findAll();
			rulePackageList = rulePackageService.findAll();
			parentServiceList = service.findAll();
			
			selectedApproverRole = entity.getApproverRole();
			selectedAdminRole = entity.getAdminRole();
			selectedHotlineRole = entity.getHotlineRole();
			selectedGroupAdminRole = entity.getGroupAdminRole();
			selectedImage = entity.getImage();
			selectedRule = entity.getAccessRule();
			selectedRulePackage = entity.getGroupFilterRulePackage();
			selectedMandatoryValuesRulePackage = entity.getMandatoryValueRulePackage();
			selectedParentService = entity.getParentService();
			
			propertyMap = new HashMap<String, String>(entity.getServiceProps());
			initialized = true;
		}
	}
	
	public String save() {
		entity.setApproverRole(selectedApproverRole);
		entity.setAdminRole(selectedAdminRole);
		entity.setHotlineRole(selectedHotlineRole);
		entity.setGroupAdminRole(selectedGroupAdminRole);
		entity.setImage(selectedImage);
		entity.setAccessRule(selectedRule);
		entity.setServiceProps(propertyMap);
		entity.setGroupFilterRulePackage(selectedRulePackage);
		entity.setMandatoryValueRulePackage(selectedMandatoryValuesRulePackage);
		entity.setParentService(selectedParentService);
		entity = service.save(entity);
		return "show-service.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public String cancel() {
		return "show-service.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public void removeProp(String key) {
		setNewKey(key);
		setNewValue(propertyMap.get(key));
		propertyMap.remove(key);
	}
	
	public void addProp() {
		if (newKey != null && newValue != null) {
			propertyMap.put(newKey, newValue);
			setNewKey(null);
			setNewValue(null);
		}
	}

	public ServiceEntity getEntity() {
		return entity;
	}

	public void setEntity(ServiceEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ApproverRoleEntity getSelectedApproverRole() {
		return selectedApproverRole;
	}

	public void setSelectedApproverRole(ApproverRoleEntity selectedApproverRole) {
		this.selectedApproverRole = selectedApproverRole;
	}

	public AdminRoleEntity getSelectedAdminRole() {
		return selectedAdminRole;
	}

	public void setSelectedAdminRole(AdminRoleEntity selectedAdminRole) {
		this.selectedAdminRole = selectedAdminRole;
	}

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}

	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}

	public List<ApproverRoleEntity> getApproverRoleList() {
		return approverRoleList;
	}

	public void setApproverRoleList(List<ApproverRoleEntity> approverRoleList) {
		this.approverRoleList = approverRoleList;
	}

	public List<AdminRoleEntity> getAdminRoleList() {
		return adminRoleList;
	}

	public void setAdminRoleList(List<AdminRoleEntity> adminRoleList) {
		this.adminRoleList = adminRoleList;
	}

	public List<ImageEntity> getImageList() {
		return imageList;
	}

	public void setImageList(List<ImageEntity> imageList) {
		this.imageList = imageList;
	}

	public ImageEntity getSelectedImage() {
		return selectedImage;
	}

	public void setSelectedImage(ImageEntity selectedImage) {
		this.selectedImage = selectedImage;
	}

	public List<BusinessRuleEntity> getRuleList() {
		return ruleList;
	}

	public void setRuleList(List<BusinessRuleEntity> ruleList) {
		this.ruleList = ruleList;
	}

	public BusinessRuleEntity getSelectedRule() {
		return selectedRule;
	}

	public void setSelectedRule(BusinessRuleEntity selectedRule) {
		this.selectedRule = selectedRule;
	}

	public AdminRoleEntity getSelectedHotlineRole() {
		return selectedHotlineRole;
	}

	public void setSelectedHotlineRole(AdminRoleEntity selectedHotlineRole) {
		this.selectedHotlineRole = selectedHotlineRole;
	}

	public List<AdminRoleEntity> getHotlineRoleList() {
		return hotlineRoleList;
	}

	public void setHotlineRoleList(List<AdminRoleEntity> hotlineRoleList) {
		this.hotlineRoleList = hotlineRoleList;
	}

	public List<GroupAdminRoleEntity> getGroupAdminRoleList() {
		return groupAdminRoleList;
	}

	public GroupAdminRoleEntity getSelectedGroupAdminRole() {
		return selectedGroupAdminRole;
	}

	public void setSelectedGroupAdminRole(
			GroupAdminRoleEntity selectedGroupAdminRole) {
		this.selectedGroupAdminRole = selectedGroupAdminRole;
	}

	public BusinessRulePackageEntity getSelectedRulePackage() {
		return selectedRulePackage;
	}

	public void setSelectedRulePackage(BusinessRulePackageEntity selectedRulePackage) {
		this.selectedRulePackage = selectedRulePackage;
	}

	public List<BusinessRulePackageEntity> getRulePackageList() {
		return rulePackageList;
	}

	public BusinessRulePackageEntity getSelectedMandatoryValuesRulePackage() {
		return selectedMandatoryValuesRulePackage;
	}

	public void setSelectedMandatoryValuesRulePackage(
			BusinessRulePackageEntity selectedMandatoryValuesRulePackage) {
		this.selectedMandatoryValuesRulePackage = selectedMandatoryValuesRulePackage;
	}

	public List<ServiceEntity> getParentServiceList() {
		return parentServiceList;
	}

	public void setParentServiceList(List<ServiceEntity> parentServiceList) {
		this.parentServiceList = parentServiceList;
	}

	public ServiceEntity getSelectedParentService() {
		return selectedParentService;
	}

	public void setSelectedParentService(ServiceEntity selectedParentService) {
		this.selectedParentService = selectedParentService;
	}
}
