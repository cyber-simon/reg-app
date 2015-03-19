package edu.kit.scc.webreg.service.reg;

import java.io.Serializable;

import edu.kit.scc.webreg.as.AttributeSourceQueryLog;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface AttributeSourceQueryService extends Serializable {

	AttributeSourceQueryLog updateUserAttributes(UserEntity user,
			AttributeSourceEntity attributeSource, String executor)
			throws RegisterException;
}
