package edu.kit.scc.webreg.service.attribute.release;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityAttributeResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AttributeBuilder {

	@Inject
	private Logger logger;

	@Inject
	private AttributeReleaseDao attributeReleaseDao;

	@Inject
	private OutgoingAttributeDao outgoingAttributeDao;

	@Inject
	private IdentityAttributeResolver attributeResolver;

	@Inject
	private ValueDao valueDao;

	public AttributeReleaseEntity requestAttributeRelease(AttributeConsumerEntity attributeConsumer,
			IdentityEntity identity) {
		final AttributeReleaseEntity attributeRelease = resolveAttributeRelease(attributeConsumer, identity);
		return attributeRelease;
	}

	public void addSingleStringAttribute(AttributeReleaseEntity attributeRelease, String name,
			IdentityEntity identity) {
		addSingleStringAttribute(attributeRelease, name, attributeResolver.resolveSingleStringValue(identity, name));
	}

	public void addSingleStringAttribute(AttributeReleaseEntity attributeRelease, String name, String value) {
		if (value != null)
			setSingleStringValue(attributeRelease, name, value);
	}

	public void addStringListAttribute(AttributeReleaseEntity attributeRelease, String name, IdentityEntity identity) {
		setStringListValue(attributeRelease, name, attributeResolver.resolveStringListValue(identity, name));
	}

	public void addStringListAttribute(AttributeReleaseEntity attributeRelease, String name, List<String> stringList) {
		setStringListValue(attributeRelease, name, stringList);
	}

	public void deleteValue(ValueEntity value) {
		value.getAttributeRelease().setChanged(true);
		value.getAttributeRelease().getValues().remove(value);
		for (ValueEntity v : value.getPrevValues()) {
			v.getNextValues().remove(value);
		}
		valueDao.delete(value);
	}
	
	private void setStringListValue(AttributeReleaseEntity attributeRelease, String name, List<String> valueList) {
		final OutgoingAttributeEntity attribute = findOrCreateOutgroingAttribute(name);
		StringListValueEntity value = (StringListValueEntity) resolveValue(attributeRelease, attribute,
				StringListValueEntity.class);
		// Null check, because with a new value it will be null
		// or the value changed
		if (value.getValueList() == null || !(new HashSet<>(value.getValueList())).equals(new HashSet<>(valueList))) {
			value.setValueList(new ArrayList<>(valueList));
			value.setChanged(true);
			attributeRelease.setChanged(true);
			attributeRelease.getValuesToDelete().remove(value);
		}
		// The value exists, but stays the same
		else {
			attributeRelease.getValuesToDelete().remove(value);
		}
	}

	private void setSingleStringValue(AttributeReleaseEntity attributeRelease, String name, String valueString) {
		final OutgoingAttributeEntity attribute = findOrCreateOutgroingAttribute(name);
		StringValueEntity value = (StringValueEntity) resolveValue(attributeRelease, attribute,
				StringValueEntity.class);
		// Null check, because with a new value it will be null
		// or the value changed
		if ((value.getValueString() == null && valueString != null)
				|| !value.getValueString().equals(valueString)) {
			// The value differs from the old value. Set the new value and set the changed
			// attribute
			value.setValueString(valueString);
			value.setChanged(true);
			attributeRelease.setChanged(true);
			attributeRelease.getValuesToDelete().remove(value);
		}
		// The value exists, but stays the same
		else {
			attributeRelease.getValuesToDelete().remove(value);
		}
	}

	private ValueEntity resolveValue(AttributeReleaseEntity attributeRelease, AttributeEntity attribute,
			Class<? extends ValueEntity> desiredClass) {
		ValueEntity value = valueDao.find(
				and(equal(ValueEntity_.attribute, attribute), equal(ValueEntity_.attributeRelease, attributeRelease)));
		if (value != null && !(desiredClass.isInstance(value))) {
			logger.info("Value type has change for attribute {}: {} -> {}", attribute.getName(),
					value.getClass().getSimpleName(), desiredClass.getSimpleName());
			for (ValueEntity v : value.getPrevValues()) {
				v.getNextValues().remove(value);
			}
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			try {
				value = desiredClass.getConstructor().newInstance();
				value.setAttribute(attribute);
				value.setAttributeRelease(attributeRelease);
				value = valueDao.persist(value);
				attributeRelease.getValues().add(value);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
					| InvocationTargetException e) {
				logger.error("Cannot create instance of class {}: {}", desiredClass.getName(), e.getMessage());
			}
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
}
