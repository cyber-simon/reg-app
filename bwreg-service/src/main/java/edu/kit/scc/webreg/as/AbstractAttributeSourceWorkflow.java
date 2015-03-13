package edu.kit.scc.webreg.as;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public abstract class AbstractAttributeSourceWorkflow implements AttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LoggerFactory.getLogger(AbstractAttributeSourceWorkflow.class);

	protected PropertyReader prop;

	public void init(ASUserAttrEntity asUserAttr)
			throws RegisterException {
		prop = new PropertyReader(asUserAttr.getAttributeSource().getAsProps());
	}
	
}
