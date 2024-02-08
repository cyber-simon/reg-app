package edu.kit.scc.webreg.service.attribute.proc;

import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class IdentityValuesProcessor {

	private List<ValueProcessor> processorList;

	public IdentityValuesProcessor(ValueUpdater valueUpdater) {
		processorList = loadProcessors();
		processorList.forEach(proc -> proc.setValueUpdater(valueUpdater));
	}

	public void addValue(ValueEntity value) {
		processorList.forEach(proc -> proc.inspectValue(value));
	}

	public void apply(IdentityAttributeSetEntity attributeSet) {
		processorList.forEach(proc -> proc.apply(attributeSet));
	}

	private List<ValueProcessor> loadProcessors() {
		return Arrays.asList(new StringListMergeValueProcessor("email_all", "email"),
				new SingleStringMergeValueProcessor("email", "email"),
				new StringListMergeValueProcessor("voperson_external_affiliation", "eduperson_affiliation"),
				new StringListMergeValueProcessor("eduperson_assurance", "eduperson_assurance"),
				new StringListMergeAuthorityValueProcessor("eduperson_entitlement", "eduperson_entitlement"),
				new SingleStringMergeValueProcessor("family_name", "family_name"),
				new SingleStringMergeValueProcessor("given_name", "given_name"));
	}
}
