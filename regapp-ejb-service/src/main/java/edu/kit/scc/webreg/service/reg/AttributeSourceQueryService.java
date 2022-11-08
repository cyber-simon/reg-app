package edu.kit.scc.webreg.service.reg;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface AttributeSourceQueryService extends Serializable {

	Boolean updateUserAttributes(UserEntity user,
			AttributeSourceEntity attributeSource, String executor)
			throws UserUpdateException;
}
