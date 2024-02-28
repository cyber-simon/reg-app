/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * ****************************************************************************
 */
package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.entity.AbstractBaseEntity_;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryEntity_;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.RegistryService;

@Stateless
public class RegistryServiceImpl extends BaseServiceImpl<RegistryEntity> implements RegistryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private RegistryDao dao;

	@Override
	public RegistryEntity findByIdWithAgreements(Long id) {
		return dao.find(equal(AbstractBaseEntity_.id, id), RegistryEntity_.agreedTexts);
	}

	@Override
	public List<RegistryEntity> findByServiceAndStatusAndIDPGood(String serviceShortName, RegistryStatus status, Date date, int limit) {
		return dao.findByServiceAndStatusAndIDPGood(serviceShortName, status, date, limit);
	}

	@Override
	public List<RegistryEntity> findRegistriesForDepro(String serviceShortName) {
		return dao.findRegistriesForDepro(serviceShortName);
	}

	@Override
	public List<RegistryEntity> findByServiceAndStatus(ServiceEntity service, RegistryStatus status) {
		return dao.findByServiceAndStatus(service, status);
	}

	@Override
	public List<RegistryEntity> findByServiceAndStatusOrderByRecon(ServiceEntity service, RegistryStatus status, int limit) {
		return dao.findAll(withLimit(limit), ascendingBy(RegistryEntity_.lastReconcile),
				and(equal(RegistryEntity_.service, service), equal(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public List<RegistryEntity> findByServiceAndNotStatus(ServiceEntity service, RegistryStatus... status) {
		return dao.findByServiceAndNotStatus(service, status);
	}

	@Override
	public RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service, UserEntity user, RegistryStatus status) {
		return dao.findByServiceAndUserAndStatus(service, user, status);
	}

	@Override
	public List<RegistryEntity> findByServiceAndIdentityAndNotStatus(ServiceEntity service, IdentityEntity identity,
			RegistryStatus... status) {
		return dao.findByServiceAndIdentityAndNotStatus(service, identity, status);
	}

	@Override
	public List<RegistryEntity> findByService(ServiceEntity service) {
		return dao.findByService(service);
	}

	@Override
	public List<RegistryEntity> findByServiceOrderByRecon(ServiceEntity service, int limit) {
		return dao.findAll(withLimit(limit), ascendingBy(RegistryEntity_.lastReconcile), equal(RegistryEntity_.service, service));
	}

	@Override
	public List<RegistryEntity> findByIdentityAndStatus(IdentityEntity identity, RegistryStatus... status) {
		return dao.findByIdentityAndStatus(identity, status);
	}

	@Override
	public List<RegistryEntity> findByIdentityAndNotStatusAndNotHidden(IdentityEntity identity, RegistryStatus... status) {
		return dao.findByIdentityAndNotStatusAndNotHidden(identity, status);
	}

	@Override
	public List<RegistryEntity> findByUser(UserEntity user) {
		return dao.findByUser(user);
	}

	@Override
	public List<RegistryEntity> findByUserAndStatus(UserEntity user, RegistryStatus... status) {
		return dao.findByUserAndStatus(user, status);
	}

	@Override
	public List<RegistryEntity> findByIdentity(IdentityEntity identity) {
		return dao.findAll(equal(RegistryEntity_.identity, identity));
	}

	@Override
	protected BaseDao<RegistryEntity> getDao() {
		return dao;
	}

}
