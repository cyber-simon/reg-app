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
import javax.persistence.Table;

@Entity(name = "SamlSpMetadataEntity")
@Table(name = "spmetadata")
public class SamlSpMetadataEntity extends SamlMetadataEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = FederationEntity.class)
	private FederationEntity federation;

	public FederationEntity getFederation() {
		return federation;
	}

	public void setFederation(FederationEntity federation) {
		this.federation = federation;
	}

}
