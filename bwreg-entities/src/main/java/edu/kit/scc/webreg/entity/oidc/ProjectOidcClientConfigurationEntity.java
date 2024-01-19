package edu.kit.scc.webreg.entity.oidc;

import edu.kit.scc.webreg.entity.project.ProjectEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "ProjectOidcClientConfigurationEntity")
@Table(name = "project_oidc_client_configuration")
public class ProjectOidcClientConfigurationEntity extends OidcClientConfigurationEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ProjectEntity.class)
	private ProjectEntity project;

	public ProjectEntity getProject() {
		return project;
	}

	public void setProject(ProjectEntity project) {
		this.project = project;
	}
}
