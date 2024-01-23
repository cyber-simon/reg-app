package edu.kit.scc.webreg.service.attribute.proc;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.UserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class MapLocalAttributeOnUserFunction extends AbstractSingularAttributePipe
		implements SingularAttributePipe<ValueEntity, ValueEntity> {

	public MapLocalAttributeOnUserFunction(ValueDao valueDao, LocalAttributeDao attributeDao,
			UserAttributeSetEntity attributeSet) {
		super(valueDao, attributeDao, attributeSet);
	}

	@Override
	public ValueEntity apply(ValueEntity in) {
		UserEntity user = ((UserAttributeSetEntity) attributeSet).getUser();
		if (in.getAttribute().getName().equals("family_name"))
			user.setSurName(((StringValueEntity) in).getValueString());
		return in;
	}
}
