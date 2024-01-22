package edu.kit.scc.webreg.service.attribute.proc;

import java.util.HashMap;
import java.util.Map;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class SamlMapLocalAttributeFunction extends AbstractSingularAttributePipe
		implements SingularAttributePipe<ValueEntity, ValueEntity> {

	private Map<String, String> nameMap;

	public SamlMapLocalAttributeFunction(ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super(valueDao, attributeDao, attributeSet);
		nameMap = new HashMap<>();
		nameMap.put("urn:oid:2.5.4.4", "family_name");
	}

	@Override
	public ValueEntity apply(ValueEntity in) {

		if (!nameMap.containsKey(in.getAttribute().getName())) {
			return in;
		}

		final String outName = nameMap.get(in.getAttribute().getName());
		final LocalAttributeEntity attributeEntity = findLocalAttributeEntity(in, outName);

		ValueEntity out = in.getNextValues().stream()
				.filter(ve -> ve.getAttribute().getName().equals(outName)).findFirst()
				.orElseGet(() -> createNewValueEntity(attributeEntity, in));

		if (in.getAttribute().getValueType().equals(ValueType.STRING))
			((StringValueEntity) out).setValueString(((StringValueEntity) in).getValueString());

		return out;
	}
}
