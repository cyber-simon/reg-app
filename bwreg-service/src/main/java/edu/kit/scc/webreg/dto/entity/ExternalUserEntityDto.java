package edu.kit.scc.webreg.dto.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ExternalUserEntityDto extends UserEntityDto {

	private static final long serialVersionUID = 1L;

	@NotNull
    @Pattern(message = "ExternalID Pattern is [a-zA-Z0-9_!#$%&*+=?{|}~.-]+",
    	regexp = "^[a-zA-Z0-9@_!#$%&*+=?{|}~.-]+$")
    @Size(min = 1, max = 1023, message = "The length of externalId should be between 1 to 1023")
	private String externalId;

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
}
