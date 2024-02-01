package edu.kit.scc.webreg.service.attribute.proc;

import java.util.List;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class MergeStringListValueFunction extends AbstractMultiAttributePipe
		implements MultiAttributePipe<List<ValueEntity>, ValueEntity> {

	public MergeStringListValueFunction(ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super(valueDao, attributeDao, attributeSet);
	}

	@Override
	public ValueEntity apply(List<ValueEntity> in) {

		return null;
	}
}
