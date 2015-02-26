package edu.kit.scc.webreg.dto.entity;

import java.io.Serializable;

public interface BaseEntityDto<PK extends Serializable> {
	PK getId();
	void setId(PK id);
}
