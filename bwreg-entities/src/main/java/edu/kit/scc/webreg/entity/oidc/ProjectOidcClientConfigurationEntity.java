package edu.kit.scc.webreg.entity.oidc;

import java.util.Set;

import edu.kit.scc.webreg.entity.project.ProjectEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "ProjectOidcClientConfigurationEntity")
@Table(name = "project_oidc_client_configuration")
public class ProjectOidcClientConfigurationEntity extends OidcClientConsumerEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ProjectEntity.class)
	private ProjectEntity project;

	@ElementCollection
	@JoinTable(name = "project_oidc_client_redirects")
    @Column(name = "value_data", length = 2048)
    private Set<String> redirects;

	public ProjectEntity getProject() {
		return project;
	}

	public void setProject(ProjectEntity project) {
		this.project = project;
	}

	public Set<String> getRedirects() {
		return redirects;
	}

	public void setRedirects(Set<String> redirects) {
		this.redirects = redirects;
	}
}
