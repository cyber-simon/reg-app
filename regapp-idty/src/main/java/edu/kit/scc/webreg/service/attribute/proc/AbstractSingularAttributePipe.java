package edu.kit.scc.webreg.service.attribute.proc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

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
			attributeEntity.setValueType(in.getAttribute().getValueType());
			attributeEntity = attributeDao.persist(attributeEntity);
		}
		
		return attributeEntity;
	}
	
	protected ValueEntity createNewValueEntity(final AttributeEntity attributeEntity, final ValueEntity in) {
		ValueEntity v = valueDao.createNew(attributeEntity.getValueType());
		v.setAttribute(attributeEntity);
		v.setAttributeSet(attributeSet);
		v = valueDao.persist(v);
		in.getNextValues().add(v);
		v.getPrevValues().add(in);
		return v;
	}
}
