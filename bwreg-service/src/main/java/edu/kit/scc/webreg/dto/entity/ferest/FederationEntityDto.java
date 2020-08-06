package edu.kit.scc.webreg.dto.entity.ferest;

import java.util.Date;

import edu.kit.scc.webreg.dto.entity.AbstractBaseEntityDto;

public class FederationEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String entityId;
	private String name;
	private Date polledAt;
	
	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getPolledAt() {
		return polledAt;
	}

	public void setPolledAt(Date polledAt) {
		this.polledAt = polledAt;
	}
}
