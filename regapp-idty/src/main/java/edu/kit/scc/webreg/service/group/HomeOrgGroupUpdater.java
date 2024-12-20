package edu.kit.scc.webreg.service.group;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface HomeOrgGroupUpdater<T extends UserEntity> {

	public Set<GroupEntity> updateGroupsForUser(T user, Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException;
}
