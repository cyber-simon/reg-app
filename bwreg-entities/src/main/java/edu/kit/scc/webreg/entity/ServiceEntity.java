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

import java.sql.Types;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;

import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

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
	
	@ManyToOne(targetEntity = SshPubKeyApproverRoleEntity.class)
	private SshPubKeyApproverRoleEntity sshPubKeyApproverRole;
	
	@ManyToOne(targetEntity = GroupAdminRoleEntity.class)
	private GroupAdminRoleEntity groupAdminRole;
	
	@ManyToOne(targetEntity = ProjectAdminRoleEntity.class)
	private ProjectAdminRoleEntity projectAdminRole;
	
	@ManyToOne(targetEntity = ImageEntity.class)
	private ImageEntity image;
	
	@OneToMany(targetEntity = PolicyEntity.class, mappedBy = "service")
	private Set<PolicyEntity> policies;

	@OneToMany(targetEntity = PolicyEntity.class, mappedBy = "projectPolicy")
	private Set<PolicyEntity> projectPolicies;

	@OneToMany(targetEntity = AttributeSourceServiceEntity.class, mappedBy = "service")
	private Set<AttributeSourceServiceEntity> attributeSourceService;

	@NotNull
	@Column(name="register_bean", length=256, nullable=false)
	private String registerBean;

	@Column(name="password_capable")
	private Boolean passwordCapable;
	
	@Column(name="group_capable")
	private Boolean groupCapable;
	
	@Column(name="project_capable")
	private Boolean projectCapable;
	
	@Column(name="ssh_pub_key_capable")
	private Boolean sshPubKeyCapable;
	
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
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String description;
	
	@Column(name = "short_description", length = 2048)
	private String shortDescription;

	@Column(name = "deregister_text")
	@Lob 
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String deregisterText;
		
	@Column(name = "published")
	private Boolean published;
	
	@Column(name = "hidden")
	private Boolean hidden;

	@OneToMany(targetEntity = ServiceAutoconnectGroupEntity.class, mappedBy = "fromService")
	private Set<ServiceAutoconnectGroupEntity> groupAutoconnectServices;

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

	public String getDeregisterText() {
		return deregisterText;
	}

	public void setDeregisterText(String deregisterText) {
		this.deregisterText = deregisterText;
	}

	public Boolean getSshPubKeyCapable() {
		return sshPubKeyCapable;
	}

	public void setSshPubKeyCapable(Boolean sshPubKeyCapable) {
		this.sshPubKeyCapable = sshPubKeyCapable;
	}

	public SshPubKeyApproverRoleEntity getSshPubKeyApproverRole() {
		return sshPubKeyApproverRole;
	}

	public void setSshPubKeyApproverRole(SshPubKeyApproverRoleEntity sshPubKeyApproverRole) {
		this.sshPubKeyApproverRole = sshPubKeyApproverRole;
	}

	public Set<ServiceAutoconnectGroupEntity> getGroupAutoconnectServices() {
		return groupAutoconnectServices;
	}

	public void setGroupAutoconnectServices(Set<ServiceAutoconnectGroupEntity> groupAutoconnectServices) {
		this.groupAutoconnectServices = groupAutoconnectServices;
	}

	public ProjectAdminRoleEntity getProjectAdminRole() {
		return projectAdminRole;
	}

	public void setProjectAdminRole(ProjectAdminRoleEntity projectAdminRole) {
		this.projectAdminRole = projectAdminRole;
	}

	public Boolean getProjectCapable() {
		return projectCapable;
	}

	public void setProjectCapable(Boolean projectCapable) {
		this.projectCapable = projectCapable;
	}

	public Set<PolicyEntity> getProjectPolicies() {
		return projectPolicies;
	}

	public void setProjectPolicies(Set<PolicyEntity> projectPolicies) {
		this.projectPolicies = projectPolicies;
	}

}
