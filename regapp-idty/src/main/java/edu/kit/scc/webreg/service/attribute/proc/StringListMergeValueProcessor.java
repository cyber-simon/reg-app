package edu.kit.scc.webreg.service.attribute.proc;

import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;

public class StringListMergeValueProcessor extends AbstractListProcessor {
	
	private String outputAttribute;
	private String[] inspectValues;
	
	public StringListMergeValueProcessor(String outputAttribute, String... inspectValues) {
		this.outputAttribute = outputAttribute;
		this.inspectValues = inspectValues;
	}
	
	public void apply(IdentityAttributeSetEntity attributeSet) {
		LocalAttributeEntity attribute = getValueUpdater().resolveAttribute(outputAttribute, ValueType.STRING_LIST);
		StringListValueEntity targetValue = (StringListValueEntity) getValueUpdater().resolveValue(attribute, attributeSet, ValueType.STRING_LIST);
		getValueUpdater().writeAsList(targetValue, getValueList());
	}
	
	@Override
	protected List<String> getInspectValueNames() {
		return Arrays.asList(inspectValues);
	}	
}
