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
package edu.kit.scc.webreg.entity;

import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;

@Entity(name = "ServiceEntity")
@Table(name = "service")
public class ServiceEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name="name", length=128, nullable=false)
	private String name;
	
	@NotNull
	@Column(name="short_name", length=32, nullable=false, unique=true)
	private String shortName;
	
	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity parentService;
	
	@ManyToOne(targetEntity = AdminRoleEntity.class)
	private AdminRoleEntity adminRole;
	
	@ManyToOne(targetEntity = AdminRoleEntity.class)
	private AdminRoleEntity hotlineRole;
	
	@ManyToOne(targetEntity = ApproverRoleEntity.class)
	private ApproverRoleEntity approverRole;
	
	@ManyToOne(targetEntity = GroupAdminRoleEntity.class)
	private GroupAdminRoleEntity groupAdminRole;
	
	@ManyToOne(targetEntity = ImageEntity.class)
	private ImageEntity image;
	
	@OneToMany(targetEntity = PolicyEntity.class, mappedBy = "service")
	private Set<PolicyEntity> policies;

	@OneToMany(targetEntity = AttributeSourceServiceEntity.class, mappedBy = "service")
	private Set<AttributeSourceServiceEntity> attributeSourceService;

	@NotNull
	@Column(name="register_bean", length=256, nullable=false)
	private String registerBean;

	@Column(name="password_capable")
	private Boolean passwordCapable;
	
	@Column(name="group_capable")
	private Boolean groupCapable;
	
	@ManyToOne(targetEntity = BusinessRuleEntity.class)
	private BusinessRuleEntity accessRule;
	
	@ManyToOne(targetEntity = BusinessRulePackageEntity.class)
	private BusinessRulePackageEntity groupFilterRulePackage;
	
	@ManyToOne(targetEntity = BusinessRulePackageEntity.class)
	private BusinessRulePackageEntity mandatoryValueRulePackage;
	
	@ElementCollection(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
	@JoinTable(name = "service_properties")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> serviceProps; 

	@Column(name = "description")
	@Lob 
	@Type(type = "org.hibernate.type.TextType")	
	private String description;
	
	@Column(name = "short_description", length = 2048)
	private String shortDescription;

	@Column(name = "published")
	private Boolean published;
	
	@Column(name = "hidden")
	private Boolean hidden;
	
	public Set<PolicyEntity> getPolicies() {
		return policies;
	}

	public void setPolicies(Set<PolicyEntity> policies) {
		this.policies = policies;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AdminRoleEntity getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(AdminRoleEntity adminRole) {
		this.adminRole = adminRole;
	}

	public ApproverRoleEntity getApproverRole() {
		return approverRole;
	}

	public void setApproverRole(ApproverRoleEntity approverRole) {
		this.approverRole = approverRole;
	}

	public Map<String, String> getServiceProps() {
		return serviceProps;
	}

	public void setServiceProps(Map<String, String> serviceProps) {
		this.serviceProps = serviceProps;
	}

	public String getRegisterBean() {
		return registerBean;
	}

	public void setRegisterBean(String registerBean) {
		this.registerBean = registerBean;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public ImageEntity getImage() {
		return image;
	}

	public void setImage(ImageEntity image) {
		this.image = image;
	}

	public BusinessRuleEntity getAccessRule() {
		return accessRule;
	}

	public void setAccessRule(BusinessRuleEntity accessRule) {
		this.accessRule = accessRule;
	}

	public Boolean getPublished() {
		return published;
	}

	public void setPublished(Boolean published) {
		this.published = published;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public AdminRoleEntity getHotlineRole() {
		return hotlineRole;
	}

	public void setHotlineRole(AdminRoleEntity hotlineRole) {
		this.hotlineRole = hotlineRole;
	}

	public Boolean getPasswordCapable() {
		return passwordCapable;
	}

	public void setPasswordCapable(Boolean passwordCapable) {
		this.passwordCapable = passwordCapable;
	}

	public Boolean getGroupCapable() {
		return groupCapable;
	}

	public void setGroupCapable(Boolean groupCapable) {
		this.groupCapable = groupCapable;
	}

	public GroupAdminRoleEntity getGroupAdminRole() {
		return groupAdminRole;
	}

	public void setGroupAdminRole(GroupAdminRoleEntity groupAdminRole) {
		this.groupAdminRole = groupAdminRole;
	}

	public BusinessRulePackageEntity getGroupFilterRulePackage() {
		return groupFilterRulePackage;
	}

	public void setGroupFilterRulePackage(
			BusinessRulePackageEntity groupFilterRulePackage) {
		this.groupFilterRulePackage = groupFilterRulePackage;
	}

	public BusinessRulePackageEntity getMandatoryValueRulePackage() {
		return mandatoryValueRulePackage;
	}

	public void setMandatoryValueRulePackage(
			BusinessRulePackageEntity mandatoryValueRulePackage) {
		this.mandatoryValueRulePackage = mandatoryValueRulePackage;
	}

	public Set<AttributeSourceServiceEntity> getAttributeSourceService() {
		return attributeSourceService;
	}

	public void setAttributeSourceService(
			Set<AttributeSourceServiceEntity> attributeSourceService) {
		this.attributeSourceService = attributeSourceService;
	}

	public ServiceEntity getParentService() {
		return parentService;
	}

	public void setParentService(ServiceEntity parentService) {
		this.parentService = parentService;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

}
