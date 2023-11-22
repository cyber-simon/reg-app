package edu.kit.scc.webreg.entity;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity(name = "ServiceBasedGroupEntity")
public class ServiceBasedGroupEntity extends GroupEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "group", targetEntity = ServiceGroupFlagEntity.class)
	private Set<ServiceGroupFlagEntity> serviceGroupFlags;

	public Set<ServiceGroupFlagEntity> getServiceGroupFlags() {
		return serviceGroupFlags;
	}

	public void setServiceGroupFlags(Set<ServiceGroupFlagEntity> serviceGroupFlags) {
		this.serviceGroupFlags = serviceGroupFlags;
	}
}
