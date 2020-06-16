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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;

@Named
@ApplicationScoped
public class JpaSshPubKeyRegistryDao extends JpaBaseDao<SshPubKeyRegistryEntity, Long> implements SshPubKeyRegistryDao {

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyRegistryEntity> findByUserAndService(Long userId, Long serviceId) {
		return em.createQuery("select e from SshPubKeyRegistryEntity e where e.registry.user.id = :userId and e.registry.service.id = :serviceId")
				.setParameter("userId", userId)
				.setParameter("serviceId", serviceId)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyRegistryEntity> findByRegistry(Long registryId) {
		return em.createQuery("select e from SshPubKeyRegistryEntity e where e.registry.id = :registryId")
				.setParameter("registryId", registryId)
				.getResultList();	
	}

	@Override
    public Class<SshPubKeyRegistryEntity> getEntityClass() {
		return SshPubKeyRegistryEntity.class;
	}
}
