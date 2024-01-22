package edu.kit.scc.webreg.service.attribute.proc;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class CopyIncomingValueFunction extends AbstractSingularAttributePipe
		implements SingularAttributePipe<ValueEntity, ValueEntity> {

	public CopyIncomingValueFunction(ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super(valueDao, attributeDao, attributeSet);
	}

	@Override
	public ValueEntity apply(ValueEntity in) {
		final LocalAttributeEntity attributeEntity = findLocalAttributeEntity(in, in.getAttribute().getName());

		ValueEntity out = in.getNextValues().stream()
				.filter(ve -> ve.getAttribute().getName().equals(in.getAttribute().getName())).findFirst()
				.orElseGet(() -> createNewValueEntity(attributeEntity, in));

		if (in.getAttribute().getValueType().equals(ValueType.STRING))
			((StringValueEntity) out).setValueString(((StringValueEntity) in).getValueString());

		return out;
	}
}
