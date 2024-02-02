package edu.kit.scc.webreg.service.attribute.proc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public abstract class AbstractListProcessor extends AbstractProcessor {

	protected List<ValueEntity> valueList = new ArrayList<>();

	protected abstract List<String> getInspectValueNames();
	
	public void inspectValue(ValueEntity value) {
		Set<String> inspectSet = new HashSet<>(getInspectValueNames());
		
		if (inspectSet.contains(value.getAttribute().getName())) {
			logger.debug("Stored value {} for processing", value.getAttribute().getName());
			getValueList().add(value);
		}
	}

	public List<ValueEntity> getValueList() {
		return valueList;
	}
}
