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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "ServiceEventEntity")
public class ServiceEventEntity extends EventEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;
	
	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}
}
