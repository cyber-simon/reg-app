/*******************************************************************************
 * Copyright (c) 2016 Sven Siebler.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 *     Sven Siebler - enable Samba functionalities
 ******************************************************************************/
package edu.kit.scc.webreg.service.reg.samba;

import java.util.Map;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public class Samba4RegisterWorkflow extends AbstractSamba4RegisterWorkflow {

	@Override
	protected String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap) {
		return "/home/" + homeId + "/" + reconMap.get("groupName") + "/" + reconMap.get("localUid");
	}

	@Override
	protected String constructLocalUid(String homeId, String homeUid, UserEntity user,
			Map<String, String> reconMap) {
		return homeId + "_" + homeUid;
	}

	@Override
	protected String constructGroupName(GroupEntity group) {
		if (group instanceof HomeOrgGroupEntity) {
			HomeOrgGroupEntity homeOrgGroup = (HomeOrgGroupEntity) group;
			if (homeOrgGroup.getPrefix() == null)
				return "_" + homeOrgGroup.getName();
			else
				return homeOrgGroup.getPrefix() + "_" + homeOrgGroup.getName();
		}
		else 
			return group.getName();
	}

	@Override
	protected Boolean isSambaEnabled() {
        // SS: Dieser Workflow basiert auf der Nutzung eines Samba 4 Domänencontrollers
        // alle LDAP Optionen und Attribute wurden dahingehen angepasst, um direkt mit dem integrierten LDAP des Samba4 zu funktionieren 
		return true;
	}
}
