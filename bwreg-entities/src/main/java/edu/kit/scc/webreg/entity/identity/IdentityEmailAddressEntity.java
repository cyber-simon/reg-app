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
package edu.kit.scc.webreg.entity.identity;

import java.util.Date;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "IdentityEmailAddressEntity")
@Table(name = "idty_email")
public class IdentityEmailAddressEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = IdentityEntity.class)
    @JoinColumn(name = "identity_id", nullable = false)
	private IdentityEntity identity;
	
	@Column(name="email_address", length=2048)
	private String emailAddress;

	@Column(name = "verified_on")
	protected Date verifiedOn;

	@Column(name = "valid_until")
	protected Date validUntil;

	@Column(name = "verification_sent")
	protected Date verificationSent;

	@Column(name = "verification_token", length=64)
	protected String verificationToken;

	@Column(name = "token_valid_until")
	protected Date tokenValidUntil;

	@Enumerated(EnumType.STRING)
    @Column(name = "email_status")
	protected EmailAddressStatus emailStatus;

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Date getVerifiedOn() {
		return verifiedOn;
	}

	public void setVerifiedOn(Date verifiedOn) {
		this.verifiedOn = verifiedOn;
	}

	public Date getVerificationSent() {
		return verificationSent;
	}

	public void setVerificationSent(Date verificationSent) {
		this.verificationSent = verificationSent;
	}

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public Date getTokenValidUntil() {
		return tokenValidUntil;
	}

	public void setTokenValidUntil(Date tokenValidUntil) {
		this.tokenValidUntil = tokenValidUntil;
	}

	public EmailAddressStatus getEmailStatus() {
		return emailStatus;
	}

	public void setEmailStatus(EmailAddressStatus emailStatus) {
		this.emailStatus = emailStatus;
	}

}
