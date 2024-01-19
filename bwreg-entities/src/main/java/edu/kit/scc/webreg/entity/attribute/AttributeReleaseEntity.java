package edu.kit.scc.webreg.entity.attribute;

import java.util.Date;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "AttributeReleaseEntity")
@Table(name = "attribute_release")
public class AttributeReleaseEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "issued_at")
	protected Date issuedAt;

	@Column(name = "valid_until")
	protected Date validUntil;

	@ManyToOne(targetEntity = AttributeConsumerEntity.class)
	private AttributeConsumerEntity attributeConsumer;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	public AttributeConsumerEntity getAttributeConsumer() {
		return attributeConsumer;
	}

	public void setAttributeConsumer(AttributeConsumerEntity attributeConsumer) {
		this.attributeConsumer = attributeConsumer;
	}

	public Date getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(Date issuedAt) {
		this.issuedAt = issuedAt;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}
}
