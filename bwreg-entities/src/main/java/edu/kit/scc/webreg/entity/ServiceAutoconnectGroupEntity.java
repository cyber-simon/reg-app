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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "ServiceAutoconnectGroupEntity")
@Table(name = "service_autoconnect_group")
public class ServiceAutoconnectGroupEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity fromService;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity toService;
	
	@Column(name = "filter_regex", length = 1024)
	private String filterRegex;

	public ServiceEntity getFromService() {
		return fromService;
	}

	public void setFromService(ServiceEntity fromService) {
		this.fromService = fromService;
	}

	public ServiceEntity getToService() {
		return toService;
	}

	public void setToService(ServiceEntity toService) {
		this.toService = toService;
	}

	public String getFilterRegex() {
		return filterRegex;
	}

	public void setFilterRegex(String filterRegex) {
		this.filterRegex = filterRegex;
	}	
}
