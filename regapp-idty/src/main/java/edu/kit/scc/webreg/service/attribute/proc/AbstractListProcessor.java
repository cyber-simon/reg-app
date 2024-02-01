package edu.kit.scc.webreg.service.attribute.proc;

import java.util.ArrayList;
import java.util.List;

import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public abstract class AbstractListProcessor extends AbstractProcessor {

	protected List<ValueEntity> valueList = new ArrayList<>();

	public List<ValueEntity> getValueList() {
		return valueList;
	}
}
