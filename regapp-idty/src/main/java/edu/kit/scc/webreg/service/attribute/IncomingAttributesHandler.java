package edu.kit.scc.webreg.service.attribute;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.IncomingAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.IncomingAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.LongValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import jakarta.inject.Inject;

public abstract class IncomingAttributesHandler<T extends IncomingAttributeEntity> {

	@Inject
	private Logger logger;

	@Inject
	protected ValueDao valueDao;

	@Inject
	protected IncomingAttributeSetDao incomingAttributeSetDao;

	protected abstract IncomingAttributeDao<T> getDao();

	public abstract void createOrUpdateAttributes(UserEntity user, Map<String, List<Object>> attributeMap);

	protected T getAttribute(String name, ValueType valueType) {
		T attribute = getDao().find(equal(AttributeEntity_.name, name));
		if (attribute == null) {
			attribute = getDao().createNew();
			attribute.setName(name);
			// Assume String. Has to be changed for other datatypes by admin
			attribute.setValueType(valueType);
			attribute = getDao().persist(attribute);
		}
		return attribute;
	}

	protected StringValueEntity createOrUpdateStringValue(IncomingAttributeSetEntity incomingAttributeSet,
			IncomingAttributeEntity attribute, String name, String attributeValue) {
		ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				equal(ValueEntity_.incomingAttributeSet, incomingAttributeSet)));

		if (value != null && !(value instanceof StringValueEntity)) {
			logger.info(
					"ValueEntity for {} is not of type STRING, but ValueType says it should be. Deleting old value.",
					name);
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			value = new StringValueEntity();
			value.setAttribute(attribute);
			value.setIncomingAttributeSet(incomingAttributeSet);
			value = valueDao.persist(value);
		}

		((StringValueEntity) value).setValueString(attributeValue);
		return ((StringValueEntity) value);
	}

	protected LongValueEntity createOrUpdateLongValue(IncomingAttributeSetEntity incomingAttributeSet,
			IncomingAttributeEntity attribute, String name, Long attributeValue) {
		ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				equal(ValueEntity_.incomingAttributeSet, incomingAttributeSet)));

		if (value != null && !(value instanceof StringValueEntity)) {
			logger.info(
					"ValueEntity for {} is not of type STRING, but ValueType says it should be. Deleting old value.",
					name);
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			value = new LongValueEntity();
			value.setAttribute(attribute);
			value.setIncomingAttributeSet(incomingAttributeSet);
			value = valueDao.persist(value);
		}

		((LongValueEntity) value).setValueLong(attributeValue);
		return ((LongValueEntity) value);
	}

	protected StringListValueEntity createOrUpdateStringListValue(IncomingAttributeSetEntity incomingAttributeSet,
			IncomingAttributeEntity attribute, String name, List<String> attributeList) {
		ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				equal(ValueEntity_.incomingAttributeSet, incomingAttributeSet)));

		if (value != null && !(value instanceof StringListValueEntity)) {
			logger.info(
					"ValueEntity for {} is not of type STRING_LIST, but ValueType says it should be. Deleting old value.",
					name);
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			value = new StringListValueEntity();
			value.setAttribute(attribute);
			value.setIncomingAttributeSet(incomingAttributeSet);
			value = valueDao.persist(value);
		}

		StringListValueEntity listValue = (StringListValueEntity) value;
		if (listValue.getValueList() == null)
			listValue.setValueList(new ArrayList<>());

		listValue.getValueList().clear();
		if (attributeList.size() > 0) {
			for (String s : attributeList) {
				listValue.getValueList().add(s);
			}
		}
		
		return listValue;
	}
	
	protected IncomingAttributeSetEntity getIncomingAttributeSet(UserEntity user) {
		IncomingAttributeSetEntity incomingAttributeSet = incomingAttributeSetDao
				.find(equal(IncomingAttributeSetEntity_.user, user));
		if (incomingAttributeSet == null) {
			incomingAttributeSet = incomingAttributeSetDao.createNew();
			incomingAttributeSet.setUser(user);
			incomingAttributeSet = incomingAttributeSetDao.persist(incomingAttributeSet);
		}
		return incomingAttributeSet;
	}

}
