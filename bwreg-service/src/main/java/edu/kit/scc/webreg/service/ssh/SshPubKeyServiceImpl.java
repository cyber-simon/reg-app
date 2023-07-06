/*******************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html Contributors: Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.ssh;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.greaterThan;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThan;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.regapp.sshkey.SshPubKeyManager;
import edu.kit.scc.regapp.sshkey.exc.SshPubKeyBlacklistedException;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyEntity_;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class SshPubKeyServiceImpl extends BaseServiceImpl<SshPubKeyEntity> implements SshPubKeyService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SshPubKeyDao dao;

	@Inject
	private SshPubKeyManager manager;

	@Override
	public List<SshPubKeyEntity> findByKey(String encodedKey) {
		return dao.findAll(equal(SshPubKeyEntity_.encodedKey, encodedKey));
	}

	@Override
	public List<SshPubKeyEntity> findByIdentityAndStatus(Long identityId, SshPubKeyStatus keyStatus) {
		return dao.findAll(and(equal("identity.id", identityId), equal(SshPubKeyEntity_.keyStatus, keyStatus)));
	}

	@Override
	public List<SshPubKeyEntity> findByIdentityAndStatusWithRegsAndUser(Long identityId, SshPubKeyStatus keyStatus) {
		return dao.findAllEagerly(and(equal("identity.id", identityId), equal(SshPubKeyEntity_.keyStatus, keyStatus)),
				SshPubKeyEntity_.sshPubKeyRegistries, SshPubKeyEntity_.user);
	}

	@Override
	public List<SshPubKeyEntity> findKeysToExpire(int limit) {
		return dao.findAll(withLimit(limit),
				and(equal(SshPubKeyEntity_.keyStatus, SshPubKeyStatus.ACTIVE), lessThan(SshPubKeyEntity_.expiresAt, new Date())));
	}

	@Override
	public List<SshPubKeyEntity> findKeysToDelete(int limit, int days) {
		Date dateNDaysBefore = Date.from(Instant.now().minus(days, DAYS));
		return dao.findAll(withLimit(limit),
				and(equal(SshPubKeyEntity_.keyStatus, SshPubKeyStatus.EXPIRED), lessThan(SshPubKeyEntity_.expiresAt, dateNDaysBefore)));
	}

	@Override
	public List<SshPubKeyEntity> findKeysToExpiryWarning(int limit, int days) {
		Date dateInNDays = Date.from(Instant.now().plus(days, DAYS));
		return dao.findAll(withLimit(limit),
				and(equal(SshPubKeyEntity_.keyStatus, SshPubKeyStatus.ACTIVE), isNull(SshPubKeyEntity_.expireWarningSent),
						greaterThan(SshPubKeyEntity_.expiresAt, new Date()), lessThan(SshPubKeyEntity_.expiresAt, dateInNDays)));
	}

	@Override
	public SshPubKeyEntity expireKey(SshPubKeyEntity entity, String executor) {
		return manager.expireKey(entity, executor);
	}

	@Override
	public SshPubKeyEntity expiryWarningKey(SshPubKeyEntity entity, String executor) {
		return manager.expiryWarningKey(entity, executor);
	}

	@Override
	public SshPubKeyEntity keyExpirySent(SshPubKeyEntity entity) {
		return manager.keyExpirySent(entity);
	}

	@Override
	public SshPubKeyEntity keyExpiryWarningSent(SshPubKeyEntity entity) {
		return manager.keyExpiryWarningSent(entity);
	}

	@Override
	public SshPubKeyEntity deleteKey(SshPubKeyEntity entity, String executor) {
		return manager.deleteKey(entity, executor, entity.getUser() != null ? entity.getUser().getLastLoginHost() : null);
	}

	@Override
	public SshPubKeyEntity deployKey(Long identityId, SshPubKeyEntity entity, String executor) throws SshPubKeyBlacklistedException {
		return manager.deployKey(identityId, entity, executor, entity.getUser() != null ? entity.getUser().getLastLoginHost() : null);
	}

	@Override
	protected BaseDao<SshPubKeyEntity> getDao() {
		return dao;
	}

}
