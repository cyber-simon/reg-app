package edu.kit.scc.unicluster.reg;

import java.util.Map;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.reg.ldap.AbstractLdapRegisterWorkflow;

public class UniclusterRegisterWorkflow extends AbstractLdapRegisterWorkflow {

	@Override
	protected String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap) {
		if (homeId.equals("ka"))
			return "/home/kit/" + reconMap.get("groupName") + "/" + reconMap.get("localUid");
		else
			return "/home/" + homeId + "/" + reconMap.get("groupName") + "/" + reconMap.get("localUid");
	}

	@Override
	protected String constructLocalUid(String homeId, String homeUid, UserEntity user,
			Map<String, String> reconMap) {
		if (homeId.equals("ka"))
			return homeUid;
		else
			return homeId + "_" + homeUid;
	}

	@Override
	protected String constructGroupName(ServiceGroupFlagEntity sgf) {
		GroupEntity group = sgf.getGroup();
		
		if (group instanceof HomeOrgGroupEntity) {
			HomeOrgGroupEntity homeOrgGroup = (HomeOrgGroupEntity) group;
			if (homeOrgGroup.getPrefix() == null)
				return "_" + homeOrgGroup.getName();
			else if (homeOrgGroup.getPrefix().equals("ka"))
				return homeOrgGroup.getName();
			else
				return homeOrgGroup.getPrefix() + "_" + homeOrgGroup.getName();
		}
		else 
			return (sgf.getGroupNameOverride() != null ? sgf.getGroupNameOverride() : group.getName()); 
	}

	@Override
	protected Boolean isSambaEnabled() {
		return false;
	}
}
