package edu.kit.scc.webreg.service.attribute.proc;

import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;

public class StringListMergeAuthorityValueProcessor extends AbstractListProcessor {

	private String outputAttribute;
	private String[] inspectValues;

	public StringListMergeAuthorityValueProcessor(String outputAttribute, String... inspectValues) {
		this.outputAttribute = outputAttribute;
		this.inspectValues = inspectValues;
	}

	public void apply(IdentityAttributeSetEntity attributeSet) {
		LocalAttributeEntity attribute = getValueUpdater().resolveAttribute(outputAttribute);
		StringListValueEntity targetValue = (StringListValueEntity) getValueUpdater().resolveValue(attribute,
				attributeSet, StringListValueEntity.class);
		getValueUpdater().writeAsAuthorityList(targetValue, getValueList());
	}

	@Override
	protected List<String> getInspectValueNames() {
		return Arrays.asList(inspectValues);
	}
}
