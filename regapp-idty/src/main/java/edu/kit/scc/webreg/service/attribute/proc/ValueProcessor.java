package edu.kit.scc.webreg.service.attribute.proc;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public interface ValueProcessor {

	public void inspectValue(ValueEntity value);
	public void apply(IdentityAttributeSetEntity attributeSet);
	public void setValueUpdater(ValueUpdater valueUpdater);
}
