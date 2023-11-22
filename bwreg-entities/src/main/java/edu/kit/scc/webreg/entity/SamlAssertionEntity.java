package edu.kit.scc.webreg.entity;

import java.sql.Types;
import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "SamlAssertionEntity")
@Table(name = "samlassertion")
public class SamlAssertionEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "assertion_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String assertionData;

	@Column(name = "valid_until")
	private Date validUntil;
	
	@ManyToOne(targetEntity = SamlUserEntity.class)
	private SamlUserEntity user;
	
	public String getAssertionData() {
		return assertionData;
	}

	public void setAssertionData(String assertionData) {
		this.assertionData = assertionData;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public SamlUserEntity getUser() {
		return user;
	}

	public void setUser(SamlUserEntity user) {
		this.user = user;
	}
}
