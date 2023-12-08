package edu.kit.scc.webreg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;

@Entity(name = "ServiceGroupFlagEntity")
@Table(name = "service_group_flag")
public class ServiceGroupFlagEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;

	@ManyToOne(targetEntity = ServiceBasedGroupEntity.class)
	private ServiceBasedGroupEntity group;
	
	@Enumerated(EnumType.STRING)
	private ServiceGroupStatus status;

	@Column(name = "group_name_override", length = 64)
	@Pattern(regexp = "^[a-z]{1}[a-z0-9-_]{0,63}$")
	private String groupNameOverride;

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public ServiceBasedGroupEntity getGroup() {
		return group;
	}

	public void setGroup(ServiceBasedGroupEntity group) {
		this.group = group;
	}

	public ServiceGroupStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceGroupStatus status) {
		this.status = status;
	}

	public String getGroupNameOverride() {
		return groupNameOverride;
	}

	public void setGroupNameOverride(String groupNameOverride) {
		this.groupNameOverride = groupNameOverride;
	}
}
