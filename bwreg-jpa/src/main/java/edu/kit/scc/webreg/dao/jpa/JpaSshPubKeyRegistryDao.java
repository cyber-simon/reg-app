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

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SshPubKeyRegistryDao;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyRegistryStatus;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.SshPubKeyUsageType;

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
	@SuppressWarnings("unchecked")
	public List<SshPubKeyRegistryEntity> findByRegistryForInteractiveLogin(Long registryId) {
		return em.createQuery("select e from SshPubKeyRegistryEntity e "
				+ "where e.registry.id = :registryId and e.keyStatus =: keyStatus and "
				+ "e.usageType = :usageType and e.sshPubKey.keyStatus =: keyStatus2 and "
				+ "(e.sshPubKey.expiresAt > :dateNow or e.sshPubKey.expiresAt is null) and "
				+ "(e.expiresAt > :dateNow or e.expiresAt is null)")
				.setParameter("registryId", registryId)
				.setParameter("keyStatus", SshPubKeyRegistryStatus.ACTIVE)
				.setParameter("usageType", SshPubKeyUsageType.INTERACTIVE)
				.setParameter("keyStatus2", SshPubKeyStatus.ACTIVE)
				.setParameter("dateNow", new Date())
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyRegistryEntity> findByRegistryForCommandLogin(Long registryId) {
		return em.createQuery("select e from SshPubKeyRegistryEntity e "
				+ "where e.registry.id = :registryId and e.keyStatus =: keyStatus and "
				+ "e.usageType = :usageType and e.sshPubKey.keyStatus =: keyStatus2 and "
				+ "(e.sshPubKey.expiresAt > :dateNow or e.sshPubKey.expiresAt is null) and "
				+ "(e.expiresAt > :dateNow or e.expiresAt is null)")
				.setParameter("registryId", registryId)
				.setParameter("keyStatus", SshPubKeyRegistryStatus.ACTIVE)
				.setParameter("usageType", SshPubKeyUsageType.COMMAND)
				.setParameter("keyStatus2", SshPubKeyStatus.ACTIVE)
				.setParameter("dateNow", new Date())
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyRegistryEntity> findByRegistryForLogin(Long registryId) {
		return em.createQuery("select e from SshPubKeyRegistryEntity e "
				+ "where e.registry.id = :registryId and e.keyStatus =: keyStatus and "
				+ "e.sshPubKey.keyStatus =: keyStatus2 and "
				+ "(e.sshPubKey.expiresAt > :dateNow or e.sshPubKey.expiresAt is null) and "
				+ "(e.expiresAt > :dateNow or e.expiresAt is null)")
				.setParameter("registryId", registryId)
				.setParameter("keyStatus", SshPubKeyRegistryStatus.ACTIVE)
				.setParameter("keyStatus2", SshPubKeyStatus.ACTIVE)
				.setParameter("dateNow", new Date())
				.getResultList();	
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyRegistryEntity> findForApproval(Long serviceId) {
		return em.createQuery("select e from SshPubKeyRegistryEntity e where e.registry.service.id = :serviceId "
				+ "and e.keyStatus = :keyStatus")
				.setParameter("serviceId", serviceId)
				.setParameter("keyStatus", SshPubKeyRegistryStatus.PENDING)
				.getResultList();	
	}

	@Override
    public Class<SshPubKeyRegistryEntity> getEntityClass() {
		return SshPubKeyRegistryEntity.class;
	}
}
