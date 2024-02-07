package edu.kit.scc.webreg.service.attribute.proc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public abstract class AbstractSingularAttributePipe implements SingularAttributePipe<ValueEntity, ValueEntity>{

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected ValueUpdater valueUpdater;
	protected ValueDao valueDao;
	protected LocalAttributeDao attributeDao;
	protected AttributeSetEntity attributeSet;

	public AbstractSingularAttributePipe(ValueUpdater valueUpdater, ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super();
		this.valueUpdater = valueUpdater;
		this.valueDao = valueDao;
		this.attributeDao = attributeDao;
		this.attributeSet = attributeSet;
	}

	protected LocalAttributeEntity findLocalAttributeEntity(final ValueEntity in, final String name) {
		LocalAttributeEntity attributeEntity = attributeDao.find(equal(LocalAttributeEntity_.name, name));
		
		if (attributeEntity == null) {
			attributeEntity = attributeDao.createNew();
			attributeEntity.setName(name);
			attributeEntity = attributeDao.persist(attributeEntity);
		}
		
		return attributeEntity;
	}
	
	protected ValueEntity persistNewValueEntity(final AttributeEntity attributeEntity, final ValueEntity in) {
		ValueEntity value;
		try {
			value = in.getClass().getConstructor().newInstance();
			value.setAttribute(attributeEntity);
			value.setAttributeSet(attributeSet);
			value = valueDao.persist(value);
			in.getNextValues().add(value);
			value.getPrevValues().add(in);
			return value;
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			logger.error("Cannot create instance of type {}: {}", in.getClass().getName(), e.getMessage());
			return null;
		} 
	}
}
