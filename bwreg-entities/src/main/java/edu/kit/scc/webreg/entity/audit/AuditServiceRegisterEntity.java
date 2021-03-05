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
package edu.kit.scc.webreg.entity.audit;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import edu.kit.scc.webreg.entity.RegistryEntity;

@Entity(name = "AuditServiceRegisterEntity")
public class AuditServiceRegisterEntity extends AuditEntryEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = RegistryEntity.class)
	private RegistryEntity registry;

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}
}
