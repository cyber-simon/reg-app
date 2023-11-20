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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.in;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity_;

@Named
@ApplicationScoped
public class JpaHomeOrgGroupDao extends JpaBaseDao<HomeOrgGroupEntity> implements HomeOrgGroupDao {

	@Override
	public HomeOrgGroupEntity findByGidNumber(Integer gid) {
		return find(equal(HomeOrgGroupEntity_.gidNumber, gid));
	}

	@Override
	public HomeOrgGroupEntity findByNameAndPrefix(String name, String prefix) {
		return find(and(equal(HomeOrgGroupEntity_.name, name), equal(HomeOrgGroupEntity_.prefix, prefix)));
	}

	@Override
	public List<HomeOrgGroupEntity> findByNameListAndPrefix(List<String> nameList, String prefix) {
		return (nameList == null || nameList.size() == 0) ? new ArrayList<HomeOrgGroupEntity>()
				: findAll(and(in(HomeOrgGroupEntity_.name, nameList), equal(HomeOrgGroupEntity_.prefix, prefix)));
	}

	@Override
	public Class<HomeOrgGroupEntity> getEntityClass() {
		return HomeOrgGroupEntity.class;
	}

}
