package edu.kit.scc.webreg.entity.attribute;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

	@Enumerated(EnumType.STRING)
    @Column(name = "release_status")
	private ReleaseStatusType releaseStatus;

	@OneToMany(mappedBy = "attributeRelease")
	private Set<ValueEntity> values = new HashSet<>(); 
	
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

	public ReleaseStatusType getReleaseStatus() {
		return releaseStatus;
	}

	public void setReleaseStatus(ReleaseStatusType releaseStatus) {
		this.releaseStatus = releaseStatus;
	}

	public Set<ValueEntity> getValues() {
		return values;
	}

	public void setValues(Set<ValueEntity> values) {
		this.values = values;
	}
}
