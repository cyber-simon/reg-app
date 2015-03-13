package edu.kit.scc.webreg.as;

import java.io.Serializable;

import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface AttributeSourceWorkflow extends Serializable {

	public void pollUserAttributes(ASUserAttrEntity asUserAttr,
			AttributeSourceAuditor auditor) throws RegisterException;
}
