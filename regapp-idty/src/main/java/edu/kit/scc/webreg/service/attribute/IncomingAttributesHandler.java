package edu.kit.scc.webreg.service.attribute;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.IncomingAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.IncomingAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.LocalUserAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.LongValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.service.attribute.proc.CopyIncomingValueFunction;
import edu.kit.scc.webreg.service.attribute.proc.MapLocalAttributeOnUserFunction;
import edu.kit.scc.webreg.service.attribute.proc.PrioOnAttributeSetFunction;
import edu.kit.scc.webreg.service.attribute.proc.ValueUpdater;
import jakarta.inject.Inject;

public abstract class IncomingAttributesHandler<T extends IncomingAttributeEntity> {

	@Inject
	private Logger logger;

	@Inject
	protected ValueDao valueDao;

	@Inject
	protected ValueUpdater valueUpdater;

	@Inject
	protected IncomingAttributeSetDao incomingAttributeSetDao;

	@Inject
	protected LocalUserAttributeSetDao localAttributeSetDao;

	@Inject
	protected LocalAttributeDao localAttributeDao;

	protected abstract IncomingAttributeDao<T> getDao();

	public abstract IncomingAttributeSetEntity createOrUpdateAttributes(UserEntity user,
			Map<String, List<Object>> attributeMap);

	protected abstract List<Function<ValueEntity, ValueEntity>> getProcessingFunctions(
			LocalUserAttributeSetEntity localAttributeSet);

	public LocalUserAttributeSetEntity processIncomingAttributeSet(IncomingAttributeSetEntity incoming) {
		LocalUserAttributeSetEntity localAttributeSet = localAttributeSetDao
				.find(equal(IncomingAttributeSetEntity_.user, incoming.getUser()));
		if (localAttributeSet == null) {
			localAttributeSet = localAttributeSetDao.createNew();
			localAttributeSet.setUser(incoming.getUser());
			localAttributeSet = localAttributeSetDao.persist(localAttributeSet);
		}
		
		// Alway reset prio on set and recalculate
		localAttributeSet.setPrio(null);
		
		List<ValueEntity> valueList = valueDao.findAll(equal(ValueEntity_.attributeSet, incoming));
		List<ValueEntity> actualLocalValueList = valueDao.findAll(equal(ValueEntity_.attributeSet, localAttributeSet));
		CopyIncomingValueFunction cvf = new CopyIncomingValueFunction(valueUpdater, valueDao, localAttributeDao, localAttributeSet);
		MapLocalAttributeOnUserFunction mlaf = new MapLocalAttributeOnUserFunction(valueUpdater, valueDao, localAttributeDao,
				localAttributeSet);
		PrioOnAttributeSetFunction poasf = new PrioOnAttributeSetFunction(valueUpdater, valueDao, localAttributeDao,
				localAttributeSet);

		for (ValueEntity value : valueList) {
			ValueEntity v = cvf.apply(value);
			actualLocalValueList.remove(v);
			for (Function<ValueEntity, ValueEntity> f : getProcessingFunctions(localAttributeSet)) {
				v = f.apply(v);
				actualLocalValueList.remove(v);
			}
			v.setEndValue(true);
			v = mlaf.apply(v);
			v = poasf.apply(v);
		}
		
//		Function<ValueEntity, ValueEntity> f = getProcessingFunctions(localAttributeSet).stream()
//				.reduce(Function.identity(), Function::andThen);
//		valueList.stream().map(cvf).map(f).map(v -> {
//			v.setEndValue(true);
//			return v;
//		}).map(mlaf).map(poasf).toList();

		// After incoming values are copied to local attribute set, inspect the values that too much
		for (ValueEntity value : actualLocalValueList) {
			logger.debug("Found {} to be an unconnected value, deleting", value.getAttribute().getName());
			valueDao.delete(value);
		}
		
		return localAttributeSet;
	}

	protected T getAttribute(String name) {
		T attribute = getDao().find(equal(AttributeEntity_.name, name));
		if (attribute == null) {
			attribute = getDao().createNew();
			attribute.setName(name);
			attribute = getDao().persist(attribute);
		}
		return attribute;
	}

	protected StringValueEntity createOrUpdateStringValue(IncomingAttributeSetEntity incomingAttributeSet,
			IncomingAttributeEntity attribute, String name, String attributeValue) {
		ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				equal(ValueEntity_.attributeSet, incomingAttributeSet)));

		if (value != null && !(value instanceof StringValueEntity)) {
			logger.info(
					"ValueEntity for {} is not of type String, but ValueType says it should be. Deleting old value.",
					name);
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			value = new StringValueEntity();
			value.setAttribute(attribute);
			value.setAttributeSet(incomingAttributeSet);
			value = valueDao.persist(value);
		}

		((StringValueEntity) value).setValueString(attributeValue);
		return ((StringValueEntity) value);
	}

	protected LongValueEntity createOrUpdateLongValue(IncomingAttributeSetEntity incomingAttributeSet,
			IncomingAttributeEntity attribute, String name, Long attributeValue) {
		ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				equal(ValueEntity_.attributeSet, incomingAttributeSet)));

		if (value != null && !(value instanceof LongValueEntity)) {
			logger.info(
					"ValueEntity for {} is not of type Long, but ValueType says it should be. Deleting old value.",
					name);
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			value = new LongValueEntity();
			value.setAttribute(attribute);
			value.setAttributeSet(incomingAttributeSet);
			value = valueDao.persist(value);
		}

		((LongValueEntity) value).setValueLong(attributeValue);
		return ((LongValueEntity) value);
	}

	protected StringListValueEntity createOrUpdateStringListValue(IncomingAttributeSetEntity incomingAttributeSet,
			IncomingAttributeEntity attribute, String name, List<String> attributeList) {
		ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				equal(ValueEntity_.attributeSet, incomingAttributeSet)));

		if (value != null && !(value instanceof StringListValueEntity)) {
			logger.info(
					"ValueEntity for {} is not of type StringList, but ValueType says it should be. Deleting old value.",
					name);
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			value = new StringListValueEntity();
			value.setAttribute(attribute);
			value.setAttributeSet(incomingAttributeSet);
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

	protected void removeValues(Map<String, ValueEntity> removeValueMap) {
		logger.debug("Found {} values to remove", removeValueMap.size());
		removeValueMap.entrySet().stream().forEach(entry -> {
			logger.debug("Try to delete entry {}", entry.getKey());
			ValueEntity value = entry.getValue();
			List<ValueEntity> deleteList = new ArrayList<>();
			deleteList.add(value);
			if (value.getNextValues().size() > 0) {
				while (value.getNextValues().iterator().hasNext()) {
					value = value.getNextValues().iterator().next();
					logger.debug("Unrolled value {}", value.getAttribute().getName());
					deleteList.add(value);
				}
			}

			deleteList.stream().forEach(v -> {
				logger.debug("Delete value {} for attribute {} from attributeset {} ({})", v.getId(), v.getAttribute().getName(),
						v.getAttributeSet().getId(), v.getAttributeSet().getClass().getSimpleName());
				valueDao.delete(v);
			});
		});
	}
	
	protected List<ValueEntity> getValueList(AttributeSetEntity attributeSet) {
		return valueDao.findAll(equal(ValueEntity_.attributeSet, attributeSet));
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
