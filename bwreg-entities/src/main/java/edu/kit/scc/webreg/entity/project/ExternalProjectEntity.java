package edu.kit.scc.webreg.entity.project;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "ExternalProjectEntity")
public class ExternalProjectEntity extends ProjectEntity {

	private static final long serialVersionUID = 1L;

	@Column(name="external_name", length=1024)
	private String externalName;

	public String getExternalName() {
		return externalName;
	}

	public void setExternalName(String externalName) {
		this.externalName = externalName;
	}

}
