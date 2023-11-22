package edu.kit.scc.webreg.entity.project;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity(name = "LocalProjectEntity")
public class LocalProjectEntity extends ProjectEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(mappedBy = "project")
	private Set<ProjectInvitationTokenEntity> invitationTokens;

	public Set<ProjectInvitationTokenEntity> getInvitationTokens() {
		return invitationTokens;
	}

	public void setInvitationTokens(Set<ProjectInvitationTokenEntity> invitationTokens) {
		this.invitationTokens = invitationTokens;
	}

}
