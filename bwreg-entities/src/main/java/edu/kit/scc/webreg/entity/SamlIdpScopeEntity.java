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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "SamlIdpScopeEntity")
@Table(name = "idpmetadata_scope")
public class SamlIdpScopeEntity extends AbstractBaseEntity {
 
	private static final long serialVersionUID = 1L;

	@Column(name = "scope", nullable = false, length = 512)
	private String scope;
	
	@Column(name = "regex")
	private Boolean regex;

	@ManyToOne(targetEntity = SamlIdpMetadataEntity.class)
	private SamlIdpMetadataEntity idp;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Boolean getRegex() {
		return regex;
	}

	public void setRegex(Boolean regex) {
		this.regex = regex;
	}

	public SamlIdpMetadataEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		this.idp = idp;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		
	    return this.getClass().equals(other.getClass()) && 
	    		(getScope() != null) && (getIdp() != null)
	         ? getScope().equals(((SamlIdpScopeEntity) other).getScope()) &&
	        		 getIdp().equals(((SamlIdpScopeEntity) other).getIdp())
	         : (other == this);
	}
	
	@Override
	public int hashCode() {
	    return (getScope() != null && getIdp() != null) 
		         ? (getClass().hashCode() + getScope().hashCode() + getIdp().hashCode())
		         : super.hashCode();
	}
	
}
