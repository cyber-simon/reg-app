package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "SamlAssertionEntity")
@Table(name = "samlassertion")
public class SamlAssertionEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "assertion_data")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
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
