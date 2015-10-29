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
package edu.kit.scc.webreg.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.RoleGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;

@Named
@ApplicationScoped
public class JpaRoleDao extends JpaBaseDao<RoleEntity, Long> implements RoleDao {

	@Override
	public UserRoleEntity createNewUserRole() {
		return new UserRoleEntity();
	}
	
	@Override
	public void persistUserRole(UserRoleEntity userRole) {
		em.persist(userRole);
	}

	@Override
	public void addUserToRole(UserEntity user, String roleName) {
		RoleEntity role = findByName(roleName);
		UserRoleEntity userRole = createNewUserRole();
		userRole.setRole(role);
		userRole.setUser(user);
		persistUserRole(userRole);
	}

	@Override
	public void addGroupToRole(GroupEntity group, RoleEntity role) {
		RoleGroupEntity roleGroup = createNewRoleGroup();
		roleGroup.setRole(role);
		roleGroup.setGroup(group);
		em.persist(roleGroup);
	}
	
	@Override
	public void removeGroupFromRole(GroupEntity group, RoleEntity role) {
		RoleGroupEntity roleGroup = findRoleGroupEntity(group, role);
		if (roleGroup != null)
			em.remove(roleGroup);
	}
	
	@Override
	public RoleGroupEntity createNewRoleGroup() {
		return new RoleGroupEntity();
	}

	@Override
	public RoleGroupEntity findRoleGroupEntity(GroupEntity group, RoleEntity role) {
		try {
			return (RoleGroupEntity) em.createQuery("select r from RoleGroupEntity r where r.role = :role "
					+ "and r.group = :group")
				.setParameter("role", role).setParameter("group", group).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void deleteUserRole(Long userId, String roleName) {
		UserRoleEntity roleEntity = (UserRoleEntity) em.createQuery("select r from UserRoleEntity r where r.user.id = :userId "
				+ "and r.role.name = :roleName")
			.setParameter("userId", userId).setParameter("roleName", roleName).getSingleResult();
		em.remove(roleEntity);
	}
	
    @SuppressWarnings("unchecked")
	@Override
	public List<RoleEntity> findByGroups(Set<GroupEntity> groups) {
    	if (groups == null || groups.isEmpty())
    		return new ArrayList<RoleEntity>();
    	
		return em.createQuery("select r.role from RoleGroupEntity r where r.group in (:groups)")
				.setParameter("groups", groups).getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<RoleEntity> findByUser(UserEntity user) {
		return em.createQuery("select r.role from UserRoleEntity r where r.user = :user")
				.setParameter("user", user).getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<RoleEntity> findByUserId(Long userId) {
		return em.createQuery("select r.role from UserRoleEntity r where r.user.id = :userId")
				.setParameter("userId", userId).getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<UserEntity> findUsersForRole(RoleEntity role) {
		return em.createQuery("select u from UserEntity u left join u.roles ur where ur.role = :role")
				.setParameter("role", role).getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public List<GroupEntity> findGroupsForRole(RoleEntity role) {
		return em.createQuery("select g from GroupEntity g left join g.roles gr where gr.role = :role")
				.setParameter("role", role).getResultList();
	}

    @SuppressWarnings("unchecked")
	@Override
	public Boolean checkUserInRole(Long userId, String roleName) {
		List<RoleEntity> roleList =  em.createQuery("select r.role from UserRoleEntity r where r.user.id = :userId and r.role.name = :roleName")
				.setParameter("userId", userId).setParameter("roleName", roleName).getResultList();
		return (roleList.size() > 0 ? Boolean.TRUE : Boolean.FALSE);
	}

    @SuppressWarnings("unchecked")
	@Override
	public Boolean checkAdminUserInRole(Long userId, String roleName) {
		List<RoleEntity> roleList =  em.createQuery("select u.roles from AdminUserEntity u where u.id = :userId")
				.setParameter("userId", userId).getResultList();
		List<RoleEntity> roleList2 =  em.createQuery("select r from RoleEntity r where r.name = :roleName and r in :roleList")
				.setParameter("roleList", roleList).setParameter("roleName", roleName).getResultList();
		return (roleList2.size() > 0 ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public RoleEntity findWithUsers(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> root = criteria.from(RoleEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		root.fetch("users", JoinType.LEFT);
		
		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public RoleEntity findByName(String name) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> criteria = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> role = criteria.from(RoleEntity.class);
		criteria.where(
				builder.equal(role.get("name"), name));
		criteria.select(role);
		
		try {		
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public Class<RoleEntity> getEntityClass() {
		return RoleEntity.class;
	}
}
