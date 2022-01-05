package edu.kit.scc.webreg.entity.project;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity(name = "LocalProjectEntity")
public class LocalProjectEntity extends ProjectEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "project")
	private Set<ProjectInvitationToken> invitationTokens;

	public Set<ProjectInvitationToken> getInvitationTokens() {
		return invitationTokens;
	}

	public void setInvitationTokens(Set<ProjectInvitationToken> invitationTokens) {
		this.invitationTokens = invitationTokens;
	}

}
