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
package edu.kit.scc.webreg.dao.jpa.project;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.project.ProjectAdminRoleDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity_;

@Named
@ApplicationScoped
public class JpaProjectAdminRoleDao extends JpaBaseDao<ProjectAdminRoleEntity> implements ProjectAdminRoleDao {

	@Override
	public List<ProjectAdminRoleEntity> findWithServices(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ProjectAdminRoleEntity> criteria = builder.createQuery(ProjectAdminRoleEntity.class);
		Root<ProjectAdminRoleEntity> root = criteria.from(ProjectAdminRoleEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);

		CriteriaQuery<ProjectAdminRoleEntity> select = criteria.select(root);
		select.where(builder.equal(userRoot.get(ProjectAdminRoleEntity_.id), user.getId())).distinct(true);
		root.fetch(ProjectAdminRoleEntity_.adminForServices, JoinType.LEFT);
		return em.createQuery(select).getResultList();
	}

	@Override
	public ProjectAdminRoleEntity findWithUsers(Long id) {
		return find(equal(ProjectAdminRoleEntity_.id, id), ProjectAdminRoleEntity_.users);
	}

	@Override
	public ProjectAdminRoleEntity findByName(String name) {
		return find(equal(ProjectAdminRoleEntity_.name, name));
	}

	@Override
	public Class<ProjectAdminRoleEntity> getEntityClass() {
		return ProjectAdminRoleEntity.class;
	}

}
