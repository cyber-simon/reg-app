package edu.kit.scc.webreg.service.attribute;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.IncomingAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.IncomingSamlAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.IncomingSamlAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IncomingAttributesHandler {

	@Inject
	private Logger logger;
	
	@Inject
	private IncomingSamlAttributeDao samlAttributeDao;
	
	@Inject
	private ValueDao valueDao;
	
	@Inject
	private IncomingAttributeSetDao incomingAttributeSetDao;
	
	public void createOrUpdateSamlAttributes(UserEntity user, Map<String, List<Object>> attributeMap) {
		IncomingAttributeSetEntity incomingAttributeSet = incomingAttributeSetDao.find(equal(IncomingAttributeSetEntity_.user, user));
		if (incomingAttributeSet == null) {
			incomingAttributeSet = incomingAttributeSetDao.createNew();
			incomingAttributeSet.setUser(user);
			incomingAttributeSet = incomingAttributeSetDao.persist(incomingAttributeSet);
		}
		
		final IncomingAttributeSetEntity incomingSet = incomingAttributeSet;
		attributeMap.entrySet().stream().forEach(entry -> createOrUpdateSamlAttribute(incomingSet, entry.getKey(), entry.getValue()));
	}
	
	public void createOrUpdateSamlAttribute(IncomingAttributeSetEntity incomingAttributeSet, String name, List<Object> attributeList) {
		 IncomingSamlAttributeEntity attribute = samlAttributeDao.find(equal(AttributeEntity_.name, name));
		 if (attribute == null) {
			 attribute = samlAttributeDao.createNew();
			 attribute.setName(name);
			 //Assume String. Has to be changed for other datatypes by admin
			 attribute.setValueType(ValueType.STRING);
			 attribute = samlAttributeDao.persist(attribute);
		 }

		 ValueEntity value = valueDao.find(RqlExpressions.and(equal(ValueEntity_.attribute, attribute),
				 equal(ValueEntity_.incomingAttributeSet, incomingAttributeSet)));

		 if (ValueType.STRING.equals(attribute.getValueType())) {
			 if (value != null && !(value instanceof StringValueEntity)) {
				 logger.info("ValueEntity for {} is not of type STRING, but ValueType says it should be. Deleting old value.", name);
				 valueDao.delete(value);
				 value = null;
			 }

			 if (value == null) {
				 value = new StringValueEntity();
				 value.setAttribute(attribute);
				 value.setIncomingAttributeSet(incomingAttributeSet);
				 value = valueDao.persist(value);
			 }

			 StringValueEntity stringValue = (StringValueEntity) value;
			 if (attributeList == null) {
				 logger.info("No value for {}", name);
			 }
			 else if (attributeList.size() == 1) {
				 // Single element
				 if (attributeList.get(0) instanceof String) {
					 String s = (String) attributeList.get(0);
					 if (! s.equals(stringValue.getValueString())) {
						 stringValue.setValueString(s);
					 }
				 }
				 else {
					 logger.warn("Value for {} ({}) is single value, but not of type String", name, attributeList.get(0).getClass().getName());
				 }
			 }
			 else {
				 logger.warn("Value for {} is not single value", name, attributeList.get(0).getClass().getName());
			 }
		 } 
		 else if (ValueType.STRING_LIST.equals(attribute.getValueType())) {
			 if (value != null && !(value instanceof StringListValueEntity)) {
				 logger.info("ValueEntity for {} is not of type STRING, but ValueType says it should be. Deleting old value.", name);
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
				 for (Object o : attributeList) {
					 if (o instanceof String)
						 listValue.getValueList().add((String) o);
				 }
			 }
		 }
	}
}
