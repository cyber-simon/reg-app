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

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "registry")
public class RegistryEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	private RegistryStatus registryStatus;
	
	@Column(name="status_messages", length=1024)
	private String statusMessage;
	
	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;
	
	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;
	
	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;

	@NotNull
	@Column(name="register_bean", length=256, nullable=false)
	private String registerBean;
	
	@Column(name="approval_bean", length=256)
	private String approvalBean;
	
	@ManyToMany(targetEntity=AgreementTextEntity.class)
	@JoinTable(name = "registry_agreementtext",
			joinColumns = @JoinColumn(name="registry_id"),
			inverseJoinColumns = @JoinColumn(name="agreementtext_id")
	)
	private Set<AgreementTextEntity> agreedTexts;
	
	@Column(name="agreed_time")
	private Date agreedTime;
	
	@ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SELECT)
	@JoinTable(name = "registry_value")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> registryValues; 
	
	@Column(name = "last_recon")
	private Date lastReconcile;
	
	@Column(name = "last_full_recon")
	private Date lastFullReconcile;
	
	@Column(name = "last_status_change")
	private Date lastStatusChange;
	
	@Column(name = "last_access_check")
	private Date lastAccessCheck;
	
	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public Date getAgreedTime() {
		return agreedTime;
	}

	public void setAgreedTime(Date agreedTime) {
		this.agreedTime = agreedTime;
	}

	public Map<String, String> getRegistryValues() {
		return registryValues;
	}

	public void setRegistryValues(Map<String, String> registryValues) {
		this.registryValues = registryValues;
	}

	public Set<AgreementTextEntity> getAgreedTexts() {
		return agreedTexts;
	}

	public void setAgreedTexts(Set<AgreementTextEntity> agreedTexts) {
		this.agreedTexts = agreedTexts;
	}

	public RegistryStatus getRegistryStatus() {
		return registryStatus;
	}

	public void setRegistryStatus(RegistryStatus registryStatus) {
		this.registryStatus = registryStatus;
	}

	public String getRegisterBean() {
		return registerBean;
	}

	public void setRegisterBean(String registerBean) {
		this.registerBean = registerBean;
	}

	public String getApprovalBean() {
		return approvalBean;
	}

	public void setApprovalBean(String approvalBean) {
		this.approvalBean = approvalBean;
	}

	public Date getLastReconcile() {
		return lastReconcile;
	}

	public void setLastReconcile(Date lastReconcile) {
		this.lastReconcile = lastReconcile;
	}

	public Date getLastFullReconcile() {
		return lastFullReconcile;
	}

	public void setLastFullReconcile(Date lastFullReconcile) {
		this.lastFullReconcile = lastFullReconcile;
	}

	public Date getLastStatusChange() {
		return lastStatusChange;
	}

	public void setLastStatusChange(Date lastStatusChange) {
		this.lastStatusChange = lastStatusChange;
	}

	public Date getLastAccessCheck() {
		return lastAccessCheck;
	}

	public void setLastAccessCheck(Date lastAccessCheck) {
		this.lastAccessCheck = lastAccessCheck;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
}
