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

import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;

@Named
@ApplicationScoped
public class JpaSshPubKeyDao extends JpaBaseDao<SshPubKeyEntity, Long> implements SshPubKeyDao {

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByUser(Long userId) {
		return em.createQuery("select e from SshPubKeyEntity e where e.user.id = :userId")
				.setParameter("userId", userId).getResultList();	
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByUserAndStatus(Long userId, SshPubKeyStatus keyStatus) {
		return em.createQuery("select e from SshPubKeyEntity e where e.user.id = :userId and e.keyStatus = :keyStatus")
				.setParameter("userId", userId)
				.setParameter("keyStatus", keyStatus)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByUserAndStatusWithRegs(Long userId, SshPubKeyStatus keyStatus) {
		return em.createQuery("select distinct e from SshPubKeyEntity e "
				+ "left join fetch e.sshPubKeyRegistries "
				+ "where e.user.id = :userId and e.keyStatus = :keyStatus")
				.setParameter("userId", userId)
				.setParameter("keyStatus", keyStatus)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByUserAndKey(Long userId, String encodedKey) {
		return em.createQuery("select e from SshPubKeyEntity e where e.user.id = :userId and e.encodedKey = :encodedKey")
				.setParameter("userId", userId)
				.setParameter("encodedKey", encodedKey)
				.getResultList();	
	}

	@Override
    public Class<SshPubKeyEntity> getEntityClass() {
		return SshPubKeyEntity.class;
	}
}
