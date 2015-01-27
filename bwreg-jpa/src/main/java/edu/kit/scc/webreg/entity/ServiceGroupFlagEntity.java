package edu.kit.scc.webreg.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
	
	
}
