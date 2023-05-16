package edu.kit.scc.webreg.entity.project;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@Entity(name = "ProjectInvitationToken")
@Table(name = "project_invitation_token")
public class ProjectInvitationTokenEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = ProjectEntity.class)
	private ProjectEntity project;

	@NotNull
	@Column(name="invitation_token", length=128, nullable=false, unique=true)
	@Pattern(regexp = "^[a-z0-9-_]{0,63}$")
	private String token;

	@Column(name = "invitation_type")
	@Enumerated(EnumType.STRING)
	private ProjectInvitationType type;

	@Column(name = "invitation_status")
	@Enumerated(EnumType.STRING)
	private ProjectInvitationStatus status;

	@Column(name="rcpt_mail", length=1024)
	private String rcptMail;
	
	@Column(name="rcpt_name", length=1024)
	private String rcptName;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity invitedIdentity;

	@Column(name="sender_name", length=1024)
	private String senderName;
	
	@Column(name="custom_message", length=1024)
	private String customMessage;
	
	@Column(name="valid_until")
	private Date validUntil;
	
	@Column(name="last_status_change")
	private Date lastStatusChange;
	
	public ProjectEntity getProject() {
		return project;
	}

	public void setProject(ProjectEntity project) {
		this.project = project;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ProjectInvitationType getType() {
		return type;
	}

	public void setType(ProjectInvitationType type) {
		this.type = type;
	}

	public String getRcptMail() {
		return rcptMail;
	}

	public void setRcptMail(String rcptMail) {
		this.rcptMail = rcptMail;
	}

	public String getRcptName() {
		return rcptName;
	}

	public void setRcptName(String rcptName) {
		this.rcptName = rcptName;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public String getCustomMessage() {
		return customMessage;
	}

	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public ProjectInvitationStatus getStatus() {
		return status;
	}

	public void setStatus(ProjectInvitationStatus status) {
		this.status = status;
	}

	public Date getLastStatusChange() {
		return lastStatusChange;
	}

	public void setLastStatusChange(Date lastStatusChange) {
		this.lastStatusChange = lastStatusChange;
	}

	public IdentityEntity getInvitedIdentity() {
		return invitedIdentity;
	}

	public void setInvitedIdentity(IdentityEntity invitedIdentity) {
		this.invitedIdentity = invitedIdentity;
	}
}
