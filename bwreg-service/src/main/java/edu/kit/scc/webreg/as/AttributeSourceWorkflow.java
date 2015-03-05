package edu.kit.scc.webreg.as;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface AttributeSourceWorkflow extends Serializable {

	public void pollUserAttributes(AttributeSourceEntity attributeSource, UserEntity user) throws RegisterException;
}
