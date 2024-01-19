package edu.kit.scc.webreg.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity(name = "SamlUserEntity")
public class SamlUserEntity extends UserEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "persistent_id", length = 1024)
	private String persistentId;

	@Column(name = "saml_subject_id", length = 1024)
	private String subjectId;

	@Column(name = "persistent_spid", length = 1024)
	private String persistentSpId;

	@Column(name = "attr_src_id", length = 1024)
	private String attributeSourcedId;

	@Column(name = "attr_src_id_name", length = 1024)
	private String attributeSourcedIdName;

	@ManyToOne(targetEntity = SamlIdpMetadataEntity.class, fetch = FetchType.LAZY)
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

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getAttributeSourcedId() {
		return attributeSourcedId;
	}

	public void setAttributeSourcedId(String attributeSourcedId) {
		this.attributeSourcedId = attributeSourcedId;
	}

	public String getAttributeSourcedIdName() {
		return attributeSourcedIdName;
	}

	public void setAttributeSourcedIdName(String attributeSourcedIdName) {
		this.attributeSourcedIdName = attributeSourcedIdName;
	}
}
