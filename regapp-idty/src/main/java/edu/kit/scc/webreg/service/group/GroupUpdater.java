package edu.kit.scc.webreg.service.group;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;

@ApplicationScoped
public class GroupUpdater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private GroupDao groupDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;

	@Inject
	private UserDao userDao;

	@Inject
	private EventSubmitter eventSubmitter;

	public void updateGroupMembers(GroupEntity group, Collection<UserEntity> newMembers) {
		Set<UserEntity> oldMembers = new HashSet<UserEntity>(userDao.findByGroup(group));

		Set<UserEntity> usersToAdd = new HashSet<UserEntity>(newMembers);
		usersToAdd.removeAll(oldMembers);
		for (UserEntity user : usersToAdd) {
			user = userDao.fetch(user.getId());
			groupDao.addUserToGroup(user, group);
		}

		Set<UserEntity> usersToRemove = new HashSet<UserEntity>(oldMembers);
		usersToRemove.removeAll(newMembers);
		for (UserEntity user : usersToRemove) {
			user = userDao.fetch(user.getId());
			groupDao.removeUserGromGroup(user, group);
		}

		if (group instanceof ServiceBasedGroupEntity) {
			ServiceBasedGroupEntity serviceBasedGroup = (ServiceBasedGroupEntity) group;
			List<ServiceGroupFlagEntity> groupFlagList = groupFlagDao.findByGroup(serviceBasedGroup);

			for (ServiceGroupFlagEntity flag : groupFlagList) {
				flag.setStatus(ServiceGroupStatus.DIRTY);
			}

			emitGroupUpdate(group, usersToRemove);
		}
	}

	public void addUserToGroup(UserEntity user, GroupEntity group, boolean emitUpdate) {
		groupDao.addUserToGroup(user, group);

		for (ServiceGroupFlagEntity flag : groupFlagDao.findByGroup((ServiceBasedGroupEntity) group)) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
		}

		if (emitUpdate) {
			emitGroupUpdate(group, null);
		}
	}

	public void removeUserFromGroup(UserEntity user, GroupEntity group, boolean emitUpdate) {
		groupDao.removeUserGromGroup(user, group);

		for (ServiceGroupFlagEntity flag : groupFlagDao.findByGroup((ServiceBasedGroupEntity) group)) {
			flag.setStatus(ServiceGroupStatus.DIRTY);
		}

		if (emitUpdate) {
			Set<UserEntity> usersToRemove = new HashSet<UserEntity>();
			usersToRemove.add(user);
			emitGroupUpdate(group, usersToRemove);
		}
	}

	protected void emitGroupUpdate(GroupEntity group, Set<UserEntity> usersToRemove) {
		HashSet<GroupEntity> gl = new HashSet<GroupEntity>();
		gl.add(group);
		MultipleGroupEvent mge = new MultipleGroupEvent(gl);
		Map<GroupEntity, Set<UserEntity>> usersToRemoveMap = new HashMap<GroupEntity, Set<UserEntity>>();
		usersToRemoveMap.put(group, usersToRemove);
		mge.setUsersToRemove(usersToRemoveMap);

		try {
			eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "TODO");
		} catch (EventSubmitException e) {
			logger.warn("Exeption", e);
		}
	}
}
