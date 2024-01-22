package edu.kit.scc.webreg.service.attribute.proc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.LongValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public abstract class AbstractSingularAttributePipe implements SingularAttributePipe<ValueEntity, ValueEntity>{

	protected ValueDao valueDao;
	protected LocalAttributeDao attributeDao;
	protected AttributeSetEntity attributeSet;

	public AbstractSingularAttributePipe(ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super();
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
		ValueEntity v = null;
		if (in.getAttribute().getValueType().equals(ValueType.STRING))
			v = new StringValueEntity();
		else if (in.getAttribute().getValueType().equals(ValueType.STRING_LIST))
			v = new StringListValueEntity();
		else if (in.getAttribute().getValueType().equals(ValueType.LONG))
			v = new LongValueEntity();
		
		v.setAttribute(attributeEntity);
		v.setAttributeSet(attributeSet);
		v = valueDao.persist(v);
		in.getNextValues().add(v);
		return v;
	}
}
