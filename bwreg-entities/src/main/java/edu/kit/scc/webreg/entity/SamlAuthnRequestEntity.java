package edu.kit.scc.webreg.entity;

import java.sql.Types;
import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;

import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "SamlAuthnRequestEntity")
@Table(name = "samlauthnrequest")
public class SamlAuthnRequestEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "authnrequest_data", columnDefinition="TEXT")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String authnrequestData;

	@ManyToOne(targetEntity = AttributeReleaseEntity.class)
	private AttributeReleaseEntity attributeRelease;

	@ManyToOne(targetEntity = SamlSpMetadataEntity.class)
	private SamlSpMetadataEntity spMetadata;
	
	@Column(name = "valid_until")
	private Date validUntil;
	
	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getAuthnrequestData() {
		return authnrequestData;
	}

	public void setAuthnrequestData(String authnrequestData) {
		this.authnrequestData = authnrequestData;
	}

	public AttributeReleaseEntity getAttributeRelease() {
		return attributeRelease;
	}

	public void setAttributeRelease(AttributeReleaseEntity attributeRelease) {
		this.attributeRelease = attributeRelease;
	}

	public SamlSpMetadataEntity getSpMetadata() {
		return spMetadata;
	}

	public void setSpMetadata(SamlSpMetadataEntity spMetadata) {
		this.spMetadata = spMetadata;
	}

}
