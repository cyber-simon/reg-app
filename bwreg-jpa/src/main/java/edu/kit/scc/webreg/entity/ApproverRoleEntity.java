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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

@Entity(name = "ApproverRoleEntity")
public class ApproverRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(targetEntity = ServiceEntity.class, mappedBy = "approverRole")
	private Set<ServiceEntity> approverForServices;

	@NotNull
	@Column(name="approval_bean", length=255)
	private String approvalBean;
	
	public Set<ServiceEntity> getApproverForServices() {
		return approverForServices;
	}

	public void setApproverForServices(Set<ServiceEntity> approverForServices) {
		this.approverForServices = approverForServices;
	}

	public String getApprovalBean() {
		return approvalBean;
	}

	public void setApprovalBean(String approvalBean) {
		this.approvalBean = approvalBean;
	}	
}
