/*
 ******************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 ******************************************************************************
 */
package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equalIgnoreCase;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNotNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThanOrEqualTo;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.UserStatus;

@Named
@ApplicationScoped
public class JpaSamlUserDao extends JpaBaseDao<SamlUserEntity> implements SamlUserDao {

	@Override
	public List<SamlUserEntity> findUsersForPseudo(Long onHoldSince, int limit) {
		return findAll(withLimit(limit), ascendingBy(UserEntity_.lastStatusChange),
				and(equal(UserEntity_.userStatus, UserStatus.ON_HOLD),
						lessThanOrEqualTo(UserEntity_.lastStatusChange, new Date(System.currentTimeMillis() - onHoldSince)),
						isNotNull(UserEntity_.eppn), isNotNull(UserEntity_.email), isNotNull(UserEntity_.givenName),
						isNotNull(UserEntity_.surName)));
	}

	@Override
	public SamlUserEntity findByPersistent(String spId, String idpId, String persistentId) {
		return find(and(equal(SamlUserEntity_.persistentSpId, spId), equal("idp.entityId", idpId),
				equalIgnoreCase(SamlUserEntity_.persistentId, persistentId)));
	}

	@Override
	public Class<SamlUserEntity> getEntityClass() {
		return SamlUserEntity.class;
	}

}
