package edu.kit.scc.webreg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "HomeOrgGroupEntity")
public class HomeOrgGroupEntity extends ServiceBasedGroupEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = SamlIdpMetadataEntity.class)
	private SamlIdpMetadataEntity idp;

	@Column(name = "group_prefix")
	private String prefix;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public SamlIdpMetadataEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		this.idp = idp;
	}
}
