package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "SamlAuthnRequestEntity")
@Table(name = "samlauthnrequest")
public class SamlAuthnRequestEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "authnrequest_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
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
