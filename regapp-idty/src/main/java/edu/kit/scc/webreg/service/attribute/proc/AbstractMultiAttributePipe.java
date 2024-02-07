package edu.kit.scc.webreg.service.attribute.proc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public abstract class AbstractMultiAttributePipe implements MultiAttributePipe<List<ValueEntity>, ValueEntity>{

	protected ValueDao valueDao;
	protected LocalAttributeDao attributeDao;
	protected AttributeSetEntity attributeSet;

	public AbstractMultiAttributePipe(ValueDao valueDao, LocalAttributeDao attributeDao,
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
			attributeEntity = attributeDao.persist(attributeEntity);
		}
		
		return attributeEntity;
	}
	
}
