package edu.kit.scc.webreg.entity;

import java.sql.Types;
import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity(name = "SamlAuthnRequestEntity")
@Table(name = "samlauthnrequest")
public class SamlAuthnRequestEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "authnrequest_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String authnrequestData;

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

}
