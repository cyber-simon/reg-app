package edu.kit.scc.webreg.entity.attribute;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity(name = "IdentityAttributeSetEntity")
public class IdentityAttributeSetEntity extends AttributeSetEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}
}
