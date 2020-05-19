package edu.kit.scc.webreg.dto.entity.ferest;

import edu.kit.scc.webreg.dto.entity.AbstractBaseEntityDto;

public class IdpEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String entityId;
	private String orgName;
	private String displayName;
	private String description;
	private String informationUrl;
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getInformationUrl() {
		return informationUrl;
	}

	public void setInformationUrl(String informationUrl) {
		this.informationUrl = informationUrl;
	}
}
