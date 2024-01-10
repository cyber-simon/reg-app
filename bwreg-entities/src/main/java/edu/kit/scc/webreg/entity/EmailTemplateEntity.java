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

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity(name = "EmailTemplateEntity")
@Table(name = "email_template")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class EmailTemplateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "tpl_name", length=256)
	private String name;
	
	@Column(name = "tpl_subject", length=512)
	private String subject;
	
	@Column(name = "tpl_body")
	@Lob 
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String body;
	
	@Column(name = "tpl_to", length=512)
	private String to;
	
	@Column(name = "tpl_from", length=512)
	private String from;
	
	@Column(name = "tpl_cc", length=512)
	private String cc;

	@Column(name = "tpl_bcc", length=512)
	private String bcc;
	
	@Column(name = "tpl_reply_to", length=512)
	private String replyTo;

	@Column(name = "signature_alias", length=512)
	private String signatureAlias;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getSignatureAlias() {
		return signatureAlias;
	}

	public void setSignatureAlias(String signatureAlias) {
		this.signatureAlias = signatureAlias;
	}

}
