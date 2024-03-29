/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public class MultipleGroupEvent extends AbstractEvent<HashSet<GroupEntity>> {

	private static final long serialVersionUID = 1L;

	private Map<GroupEntity, Set<UserEntity>> usersToRemove;

	public MultipleGroupEvent(Set<GroupEntity> groupList) {
		super(new HashSet<>(groupList));
	}

	public Map<GroupEntity, Set<UserEntity>> getUsersToRemove() {
		return usersToRemove;
	}

	public void setUsersToRemove(Map<GroupEntity, Set<UserEntity>> usersToRemove) {
		this.usersToRemove = usersToRemove;
	}
}
