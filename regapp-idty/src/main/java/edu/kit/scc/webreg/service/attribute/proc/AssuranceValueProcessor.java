package edu.kit.scc.webreg.service.attribute.proc;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class AssuranceValueProcessor extends AbstractListProcessor {
	
	public void inspectValue(ValueEntity value) {
		if (value.getAttribute().getName().equals("eduperson_assurance")) {
			logger.debug("Stored value {} for processing", value.getAttribute().getName());
			getValueList().add(value);
		}
	}
	
	public void apply(IdentityAttributeSetEntity attributeSet) {
		LocalAttributeEntity attribute = getValueUpdater().resolveAttribute("eduperson_assurance", ValueType.STRING_LIST);
		StringListValueEntity targetValue = (StringListValueEntity) getValueUpdater().resolveValue(attribute, attributeSet, ValueType.STRING_LIST);
		getValueUpdater().writeAsList(targetValue, getValueList());
	}
}
