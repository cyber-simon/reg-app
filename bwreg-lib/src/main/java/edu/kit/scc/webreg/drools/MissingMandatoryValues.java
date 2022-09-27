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
package edu.kit.scc.webreg.drools;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.RegistryEntity;

public class MissingMandatoryValues implements Serializable {

	private static final long serialVersionUID = 1L;

	private RegistryEntity registry;
	
	public MissingMandatoryValues(RegistryEntity registry) {
		super();
		this.registry = registry;
	}

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}

}
