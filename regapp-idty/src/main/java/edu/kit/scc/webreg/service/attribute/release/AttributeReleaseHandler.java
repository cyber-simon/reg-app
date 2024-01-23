package edu.kit.scc.webreg.service.attribute.release;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.AttributeReleaseDao;
import edu.kit.scc.webreg.dao.jpa.attribute.OutgoingAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeConsumerEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity_;
import edu.kit.scc.webreg.entity.attribute.OutgoingAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.OutgoingAttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.ReleaseStatusType;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.PairwiseIdentifierValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AttributeReleaseHandler {

	@Inject
	private Logger logger;

	@Inject
	private AttributeReleaseDao attributeReleaseDao;

	@Inject
	private OutgoingAttributeDao outgoingAttributeDao;

	@Inject
	private ValueDao valueDao;

	public void calculateOidcValues(AttributeReleaseEntity attributeRelease, OidcFlowStateEntity flowState) {
		IdentityEntity identity = attributeRelease.getIdentity();
		List<String> scopeList = Arrays.asList(flowState.getScope().split(" "));
		for (String scope : scopeList) {
			logger.debug("Check scope {}", scope);
			if (scope.equals("openid")) {
				final OutgoingAttributeEntity attribute = findOrCreateOutgroingAttribute("sub");
				attribute.setValueType(ValueType.PAIRWISE_ID);
				ValueEntity value = findOrCreateValue(attributeRelease, attribute);
				((PairwiseIdentifierValueEntity) value).setValueIdentifier(UUID.randomUUID().toString());
				((PairwiseIdentifierValueEntity) value).setValueScope("unknown.org");
				((PairwiseIdentifierValueEntity) value).setAttributeConsumerEntity(flowState.getClientConfiguration());
			}
			else if (scope.equals("email")) {
				final OutgoingAttributeEntity attribute = findOrCreateOutgroingAttribute("email");
				attribute.setValueType(ValueType.STRING);
				ValueEntity value = findOrCreateValue(attributeRelease, attribute);
				((StringValueEntity) value).setValueString(identity.getPrefUser().getEmail());
			} else if (scope.equals("profile")) {
			}
		}
	}

	public AttributeReleaseEntity requestAttributeRelease(AttributeConsumerEntity attributeConsumer,
			IdentityEntity identity) {
		final AttributeReleaseEntity attributeRelease = resolveAttributeRelease(attributeConsumer, identity);
		return attributeRelease;
	}

	private AttributeReleaseEntity resolveAttributeRelease(AttributeConsumerEntity attributeConsumer,
			IdentityEntity identity) {
		AttributeReleaseEntity attributeRelease = attributeReleaseDao
				.find(and(equal(AttributeReleaseEntity_.attributeConsumer, attributeConsumer),
						equal(AttributeReleaseEntity_.identity, identity)));

		if (attributeRelease == null) {
			attributeRelease = attributeReleaseDao.createNew();
			attributeRelease.setIdentity(identity);
			attributeRelease.setAttributeConsumer(attributeConsumer);
			attributeRelease.setReleaseStatus(ReleaseStatusType.NEW);
			attributeRelease = attributeReleaseDao.persist(attributeRelease);
		}

		return attributeRelease;
	}

	private ValueEntity findOrCreateValue(AttributeReleaseEntity attributeRelease, AttributeEntity attribute) {
		ValueEntity value = valueDao.find(
				and(equal(ValueEntity_.attribute, attribute), equal(ValueEntity_.attributeRelease, attributeRelease)));
		if (value == null) {
			if (ValueType.STRING.equals(attribute.getValueType()))
				value = new StringValueEntity();
			else if (ValueType.PAIRWISE_ID.equals(attribute.getValueType()))
				value = new PairwiseIdentifierValueEntity();
			else
				value = valueDao.createNew();
			value.setAttribute(attribute);
			value.setAttributeRelease(attributeRelease);
			value = valueDao.persist(value);
		}
		return value;
	}

	private OutgoingAttributeEntity findOrCreateOutgroingAttribute(String name) {
		OutgoingAttributeEntity attribute = outgoingAttributeDao.find(equal(OutgoingAttributeEntity_.name, name));
		if (attribute == null) {
			attribute = outgoingAttributeDao.createNew();
			attribute.setName(name);
			attribute = outgoingAttributeDao.persist(attribute);
		}
		return attribute;
	}
}
