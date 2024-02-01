package edu.kit.scc.webreg.service.attribute.proc;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class CopyIncomingValueFunction extends AbstractSingularAttributePipe
		implements SingularAttributePipe<ValueEntity, ValueEntity> {

	public CopyIncomingValueFunction(ValueUpdater valueUpdater, ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super(valueUpdater, valueDao, attributeDao, attributeSet);
	}

	@Override
	public ValueEntity apply(ValueEntity in) {
		logger.debug("Copy value of attribute {} to local value", in.getAttribute().getName());
		final LocalAttributeEntity attributeEntity = findLocalAttributeEntity(in, in.getAttribute().getName());

		ValueEntity out = in.getNextValues().stream()
				.filter(ve -> ve.getAttribute().getName().equals(in.getAttribute().getName())).findFirst()
				.orElseGet(() -> createNewValueEntity(attributeEntity, in));

		valueUpdater.copyValue(in, out);

		in.setEndValue(false);
		return out;
	}
}
