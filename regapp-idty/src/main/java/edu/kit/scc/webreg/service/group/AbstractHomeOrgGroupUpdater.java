package edu.kit.scc.webreg.service.group;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public abstract class AbstractHomeOrgGroupUpdater<T extends UserEntity> implements HomeOrgGroupUpdater<T>, Serializable {

	private static final long serialVersionUID = 1L;

	public abstract Set<GroupEntity> updateGroupsForUser(T user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException;
}
