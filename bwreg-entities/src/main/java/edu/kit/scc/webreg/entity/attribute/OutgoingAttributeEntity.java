package edu.kit.scc.webreg.entity.attribute;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity(name = "OutgoingAttributeEntity")
public class OutgoingAttributeEntity extends AttributeEntity {

	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
    @Column(name = "transcoder_type")
	private TranscoderType transcoderType;

	public TranscoderType getTranscoderType() {
		return transcoderType;
	}

	public void setTranscoderType(TranscoderType transcoderType) {
		this.transcoderType = transcoderType;
	}


}
