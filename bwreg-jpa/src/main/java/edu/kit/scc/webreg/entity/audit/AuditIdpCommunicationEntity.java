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

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

@Entity(name = "AuditIdpCommunicationEntity")
public class AuditIdpCommunicationEntity extends AuditEntryEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = SamlIdpMetadataEntity.class)
	private SamlIdpMetadataEntity idp;

	@ManyToOne(targetEntity = SamlSpConfigurationEntity.class)
	private SamlSpConfigurationEntity spConfig;

	public SamlIdpMetadataEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		this.idp = idp;
	}

	public SamlSpConfigurationEntity getSpConfig() {
		return spConfig;
	}

	public void setSpConfig(SamlSpConfigurationEntity spConfig) {
		this.spConfig = spConfig;
	}

}
