package edu.kit.scc.webreg.dto.entity;

import java.io.Serializable;
import java.util.Date;

public abstract class AbstractBaseEntityDto implements BaseEntityDto<Long>, Serializable {

	private static final long serialVersionUID = 1L;

	protected Long id;
	
	protected Date createdAt;

	protected Date updatedAt;
	
	protected Integer version;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

}
