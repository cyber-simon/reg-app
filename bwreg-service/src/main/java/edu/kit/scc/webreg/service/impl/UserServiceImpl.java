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
package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserServiceHook;

@Stateless
public class UserServiceImpl extends BaseServiceImpl<UserEntity, Long> implements UserService, Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Logger logger;
	
	@Inject
	private UserDao dao;
	
	@Inject
	private SamlIdpMetadataDao idpDao;
	
	@Inject
	private SerialService serialService;
	
	@Inject
	private HookManager hookManager;

	@Inject
	private AttributeMapHelper attrHelper;
	
	@Override
	public List<UserEntity> findOrderByUpdatedWithLimit(Date date, Integer limit) {
		return dao.findOrderByUpdatedWithLimit(date, limit);
	}

	@Override
	public List<UserEntity> findOrderByFailedUpdateWithLimit(Date date, Integer limit) {
		return dao.findOrderByFailedUpdateWithLimit(date, limit);
	}

    @Override
	public List<UserEntity> findGenericStoreKeyWithLimit(String key, Integer limit) {
		return dao.findGenericStoreKeyWithLimit(key, limit);
	}
	
	@Override
	public UserEntity findByPersistentWithRoles(String spId, String idpId, String persistentId) {
		return dao.findByPersistentWithRoles(spId, idpId, persistentId);
	}

	@Override
	public List<UserEntity> findByGroup(GroupEntity group) {
		return dao.findByGroup(group);
	}
	
	@Override
	public UserEntity findByEppn(String eppn) {
		return dao.findByEppn(eppn);
	}

	@Override
	public UserEntity findByIdWithAll(Long id) {
		return dao.findByIdWithAll(id);
	}

	@Override
	public UserEntity findByIdWithStore(Long id) {
		return dao.findByIdWithStore(id);
	}

	@Override
	public boolean updateUserFromAttribute(UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor) 
				throws RegisterException {
		return updateUserFromAttribute(user, attributeMap, false, auditor);
	}

	@Override
	public boolean updateUserFromAttribute(UserEntity user, Map<String, List<Object>> attributeMap, boolean withoutUidNumber, Auditor auditor) 
				throws RegisterException {

		boolean changed = false;
		
		UserServiceHook completeOverrideHook = null;
		Set<UserServiceHook> activeHooks = new HashSet<UserServiceHook>();
		
		for (UserServiceHook hook : hookManager.getUserHooks()) {
			if (hook.isResponsible(user, attributeMap)) {
				
				hook.preUpdateUserFromAttribute(user, attributeMap, auditor);
				activeHooks.add(hook);
				
				if (hook.isCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}
		
		if (completeOverrideHook == null) {
			changed |= compareAndChangeProperty(user, "email", attributeMap.get("urn:oid:0.9.2342.19200300.100.1.3"), auditor);
			changed |= compareAndChangeProperty(user, "eppn", attributeMap.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"), auditor);
			changed |= compareAndChangeProperty(user, "givenName", attributeMap.get("urn:oid:2.5.4.42"), auditor);
			changed |= compareAndChangeProperty(user, "surName", attributeMap.get("urn:oid:2.5.4.4"), auditor);

			List<String> emailList = attrHelper.attributeListToStringList(attributeMap, "urn:oid:0.9.2342.19200300.100.1.3");
			if (emailList != null && emailList.size() > 1) {
				
				if (user.getEmailAddresses() == null) {
					user.setEmailAddresses(new HashSet<String>());
				}
				
				for (int i=1; i<emailList.size(); i++) {
					user.getEmailAddresses().add(emailList.get(i));
				}
			}
			
			if ((! withoutUidNumber) && (user.getUidNumber() == null)) {
				user.setUidNumber(serialService.next("uid-number-serial").intValue());
				logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
				auditor.logAction(user.getEppn(), "SET FIELD", "uidNumber", "" + user.getUidNumber(), AuditStatus.SUCCESS);
				changed = true;
			}
		}
		else {
			logger.info("Overriding standard User Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
		}
		
		for (UserServiceHook hook : activeHooks) {
			hook.postUpdateUserFromAttribute(user, attributeMap, auditor);
		}

		return changed;
	}

	
	private boolean compareAndChangeProperty(UserEntity user, String property, List<Object> objectValue, Auditor auditor) {
		String s = null;
		String action = null;
		
		// In case of a List (multiple SAML Values), take the first value
		String value = attrHelper.getSingleStringFirst(objectValue);
		
		try {
			Object actualValue = PropertyUtils.getProperty(user, property);

			if (actualValue != null && actualValue.equals(value)) {
				// Value didn't change, do nothing
				return false;
			}
			
			if (actualValue == null) {
				s = "null";
				action = "SET FIELD";
			}
			else {
				s = actualValue.toString();
				action = "UPDATE FIELD";
			}
			
			s = s + " -> " + value;
			if (s.length() > 1017) s = s.substring(0, 1017) + "...";
			
			PropertyUtils.setProperty(user, property, value);
			
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.SUCCESS);
		} catch (IllegalAccessException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (InvocationTargetException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (NoSuchMethodException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		}
		
		return true;
	}
	
	@Override
	protected BaseDao<UserEntity, Long> getDao() {
		return dao;
	}

	@Override
	public void convertLegacyUsers() {
		List<UserEntity> userList = dao.findLegacyUsers();
    	
		if (userList.size() > 0) {
    		logger.warn("Legacy Users found. Converting...");
    		Map<String, SamlIdpMetadataEntity> idpCache = new HashMap<String, SamlIdpMetadataEntity>();
	    	for (UserEntity user : userList) {
	    		logger.info("Converting user {}", user.getEppn());
	    		if (! idpCache.containsKey(user.getPersistentIdpId())) {
	    			idpCache.put(user.getPersistentIdpId(), idpDao.findByEntityId(user.getPersistentIdpId()));
	    		}
	    		
	    		user.setIdp(idpCache.get(user.getPersistentIdpId()));
	    		user = dao.persist(user);
	    	}
		}
	}
}
