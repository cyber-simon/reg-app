package edu.kit.scc.webreg.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity(name = "SamlUserEntity")
public class SamlUserEntity extends UserEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "persistent_id", length = 1024)
	private String persistentId;
	
	@Column(name = "persistent_spid", length = 1024)
	private String persistentSpId;
	
	@Column(name = "persistent_idpid", length = 1024)
	private String persistentIdpId;

	@ManyToOne(targetEntity = SamlIdpMetadataEntity.class)
	private SamlIdpMetadataEntity idp;
	
	@OneToMany(targetEntity = SamlAssertionEntity.class, mappedBy="user")
	private Set<SamlAssertionEntity> assertions;

	public String getPersistentId() {
		return persistentId;
	}

	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}

	public String getPersistentSpId() {
		return persistentSpId;
	}

	public void setPersistentSpId(String persistentSpId) {
		this.persistentSpId = persistentSpId;
	}

	@Deprecated
	public String getPersistentIdpId() {
		return persistentIdpId;
	}

	@Deprecated
	public void setPersistentIdpId(String persistentIdpId) {
		this.persistentIdpId = persistentIdpId;
	}

	public SamlIdpMetadataEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		this.idp = idp;
	}

	public Set<SamlAssertionEntity> getAssertions() {
		return assertions;
	}

	public void setAssertions(Set<SamlAssertionEntity> assertions) {
		this.assertions = assertions;
	}
}
