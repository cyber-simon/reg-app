/*******************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html Contributors: Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.RoleEntity_;
import edu.kit.scc.webreg.entity.RoleGroupEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@Named
@ApplicationScoped
public class JpaRoleDao extends JpaBaseDao<RoleEntity> implements RoleDao {

	@Inject
	private IdentityDao identityDao;

	@Override
	public void persistUserRole(UserRoleEntity userRole) {
		em.persist(userRole);
	}

	@Override
	public void addUserToRole(UserEntity user, String roleName) {
		RoleEntity role = findByName(roleName);
		UserRoleEntity userRole = new UserRoleEntity();
		userRole.setRole(role);
		userRole.setUser(user);
		persistUserRole(userRole);
	}

	@Override
	public void addGroupToRole(GroupEntity group, RoleEntity role) {
		RoleGroupEntity roleGroup = new RoleGroupEntity();
		roleGroup.setRole(role);
		roleGroup.setGroup(group);
		em.persist(roleGroup);
	}

	@Override
	public void removeGroupFromRole(GroupEntity group, RoleEntity role) {
		RoleGroupEntity roleGroup = findRoleGroupEntity(group, role);
		if (roleGroup != null) {
			em.remove(roleGroup);
		}
	}

	@Override
	public RoleGroupEntity findRoleGroupEntity(GroupEntity group, RoleEntity role) {
		try {
			return em.createQuery("select r from RoleGroupEntity r where r.role.id = :roleId " + "and r.group.id = :groupId", RoleGroupEntity.class)
					.setParameter("roleId", role.getId())
					.setParameter("groupId", group.getId())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void deleteUserRole(Long userId, String roleName) {
		UserRoleEntity roleEntity = em
				.createQuery("select r from UserRoleEntity r where r.user.id = :userId " + "and r.role.name = :roleName",
						UserRoleEntity.class)
				.setParameter("userId", userId)
				.setParameter("roleName", roleName)
				.getSingleResult();
		em.remove(roleEntity);
	}

	@Override
	public List<RoleEntity> findByGroups(Set<GroupEntity> groups) {
		if (groups == null || groups.isEmpty()) {
			return new ArrayList<>();
		}

		return em.createQuery("select r.role from RoleGroupEntity r where r.group in (:groups)", RoleEntity.class)
				.setParameter("groups", groups)
				.getResultList();
	}

	@Override
	public List<RoleEntity> findByUser(UserEntity user) {
		return em.createQuery("select r.role from UserRoleEntity r where r.user = :user", getEntityClass())
				.setParameter("user", user)
				.getResultList();
	}

	@Override
	public List<RoleEntity> findByUserId(PaginateBy paginateBy, Long userId) {
		TypedQuery<RoleEntity> query = em
				.createQuery("select r.role from UserRoleEntity r left join fetch r.role.adminForGroups where r.user.id = :userId",
						getEntityClass())
				.setParameter("userId", userId);
		if (paginateBy != null) {
			applyPaging(query, paginateBy);
		}
		return query.getResultList();
	}

	@Override
	public Number countAllByUserId(Long userId) {
		return em.createQuery("select count(r.role) from UserRoleEntity r where r.user.id = :userId", Number.class)
				.setParameter("userId", userId)
				.getSingleResult();
	}

	@Override
	public List<RoleEntity> findByIdentityId(Long identityId) {
		List<RoleEntity> roleList = em
				.createQuery("select r.role from IdentityRoleEntity r where r.identity.id = :identityId", RoleEntity.class)
				.setParameter("identityId", identityId)
				.getResultList();

		IdentityEntity identity = identityDao.fetch(identityId);
		roleList.addAll(em.createQuery("select r.role from UserRoleEntity r where r.user in :userIdList", RoleEntity.class)
				.setParameter("userIdList", identity.getUsers())
				.getResultList());

		return roleList;
	}

	@Override
	public List<UserEntity> findUsersForRole(RoleEntity role) {
		return em.createQuery("select u from UserEntity u left join u.roles ur where ur.role.id = :roleId", UserEntity.class)
				.setParameter("roleId", role.getId())
				.getResultList();
	}

	@Override
	public List<GroupEntity> findGroupsForRole(RoleEntity role) {
		return em.createQuery("select g from GroupEntity g left join g.roles gr where gr.role.id = :roleId", GroupEntity.class)
				.setParameter("roleId", role.getId())
				.getResultList();
	}

	@Override
	public List<SamlIdpMetadataEntity> findIdpsForRole(RoleEntity role) {
		return em
				.createQuery("select g from SamlIdpMetadataEntity g left join g.adminRoles gr where gr.role.id = :roleId",
						SamlIdpMetadataEntity.class)
				.setParameter("roleId", role.getId())
				.getResultList();
	}

	@Override
	public Boolean checkUserInRole(Long userId, String roleName) {
		return em.createQuery("select r.role from UserRoleEntity r where r.user.id = :userId and r.role.name = :roleName", RoleEntity.class)
				.setParameter("userId", userId)
				.setParameter("roleName", roleName)
				.getResultList()
				.size() > 0;
	}

	@Override
	public Boolean checkAdminUserInRole(Long userId, String roleName) {
		@SuppressWarnings("unchecked")
		List<RoleEntity> roleList = em.createQuery("select u.roles from AdminUserEntity u where u.id = :userId")
				.setParameter("userId", userId)
				.getResultList();
		return roleList.size() > 0
				&& em.createQuery("select r from RoleEntity r where r.name = :roleName and r in :roleList", RoleEntity.class)
						.setParameter("roleList", roleList)
						.setParameter("roleName", roleName)
						.getResultList()
						.size() > 0;
	}

	@Override
	public RoleEntity findByName(String name) {
		return find(equal(RoleEntity_.name, name));
	}

	@Override
	public Class<RoleEntity> getEntityClass() {
		return RoleEntity.class;
	}

}
