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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;

@Named
@ApplicationScoped
public class JpaSshPubKeyDao extends JpaBaseDao<SshPubKeyEntity> implements SshPubKeyDao {

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByIdentity(Long identityId) {
		return em.createQuery("select e from SshPubKeyEntity e where e.identity.id = :identityId")
				.setParameter("identityId", identityId).getResultList();	
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<SshPubKeyEntity> findMissingIdentity() {
		return em.createQuery("select r from SshPubKeyEntity r where r.identity is null")
				.getResultList();
	}	

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByIdentityAndStatus(Long identityId, SshPubKeyStatus keyStatus) {
		return em.createQuery("select e from SshPubKeyEntity e where e.identity.id = :identityId and e.keyStatus = :keyStatus")
				.setParameter("identityId", identityId)
				.setParameter("keyStatus", keyStatus)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByIdentityAndStatusWithRegs(Long identityId, SshPubKeyStatus keyStatus) {
		return em.createQuery("select distinct e from SshPubKeyEntity e "
				+ "left join fetch e.sshPubKeyRegistries "
				+ "where e.identity.id = :identityId and e.keyStatus = :keyStatus")
				.setParameter("identityId", identityId)
				.setParameter("keyStatus", keyStatus)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByIdentityAndKey(Long identityId, String encodedKey) {
		return em.createQuery("select e from SshPubKeyEntity e where e.identity.id = :identityId and e.encodedKey = :encodedKey")
				.setParameter("identityId", identityId)
				.setParameter("encodedKey", encodedKey)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByIdentityAndExpiryInDays(Long identityId, Integer days) {
		Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, days);
        Date expiryDatePlusN = c.getTime();
		return em.createQuery("select e from SshPubKeyEntity e where e.identity.id = :identityId and e.expiresAt < :expiryDatePlusN")
				.setParameter("identityId", identityId)
				.setParameter("expiryDatePlusN", expiryDatePlusN)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByExpiryInDays(Integer days) {
		Date currentDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.DATE, days);
        Date expiryDatePlusN = c.getTime();
		return em.createQuery("select e from SshPubKeyEntity e where e.expiresAt < :expiryDatePlusN")
				.setParameter("expiryDatePlusN", expiryDatePlusN)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findByKey(String encodedKey) {
		return em.createQuery("select e from SshPubKeyEntity e where e.encodedKey = :encodedKey")
				.setParameter("encodedKey", encodedKey)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findKeysToExpire(int limit) {
		return em.createQuery("select e from SshPubKeyEntity e where e.expiresAt < :dateNow and e.keyStatus = :keyStatus")
				.setParameter("dateNow", new Date())
				.setParameter("keyStatus", SshPubKeyStatus.ACTIVE)
				.setMaxResults(limit)
				.getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SshPubKeyEntity> findKeysToExpiryWarning(int limit, int days) {
		Date dateDays = Date.from(LocalDateTime.now().plusDays(days).atZone(ZoneId.systemDefault()).toInstant());
		return em.createQuery("select e from SshPubKeyEntity e where e.expireWarningSent = null and "
				+ "e.expiresAt > :dateNow and e.expiresAt < :dateDays and e.keyStatus = :keyStatus")
				.setParameter("dateNow", new Date())
				.setParameter("dateDays", dateDays)
				.setParameter("keyStatus", SshPubKeyStatus.ACTIVE)
				.setMaxResults(limit)
				.getResultList();	
	}

	@Override
    public Class<SshPubKeyEntity> getEntityClass() {
		return SshPubKeyEntity.class;
	}
}
