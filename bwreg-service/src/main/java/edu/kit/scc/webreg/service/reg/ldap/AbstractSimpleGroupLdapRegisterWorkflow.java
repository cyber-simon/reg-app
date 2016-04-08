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
package edu.kit.scc.webreg.service.reg.ldap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.GroupCapable;
import edu.kit.scc.webreg.service.reg.impl.GroupUpdateStructure;

public abstract class AbstractSimpleGroupLdapRegisterWorkflow 
		extends AbstractSimpleLdapRegisterWorkflow
		implements GroupCapable {

	protected static Logger logger = LoggerFactory.getLogger(AbstractSimpleLdapRegisterWorkflow.class);
	
	protected abstract String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructLocalUid(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructGroupName(GroupEntity group);
	protected abstract Boolean isSambaEnabled();
	
	@Override
	public void updateGroups(ServiceEntity service, GroupUpdateStructure updateStruct, Auditor auditor)
			throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());

		for (GroupEntity group : updateStruct.getGroups()) {
			long a = System.currentTimeMillis();
			Set<UserEntity> users = updateStruct.getUsersForGroup(group);
			
			logger.debug("Update Ldap Group for group {} and Service {}", group.getName(), service.getName());

			Set<String> memberUids = new HashSet<String>(users.size());

			Map<String, String> reconMap = new HashMap<String, String>();

			for (UserEntity user : users) {
				String homeId = user.getAttributeStore().get("http://bwidm.de/bwidmOrgId");
				String homeUid = user.getAttributeStore().get("urn:oid:0.9.2342.19200300.100.1.1");

				//Skip group member with incomplete data
				if (homeId != null && homeUid != null) {
					homeId = homeId.toLowerCase();
					memberUids.add(constructLocalUid(homeId, homeUid, user, reconMap));
				}
			}
			
			a = System.currentTimeMillis();
			ldapWorker.reconGroup(constructGroupName(group), "" + group.getGidNumber(), memberUids);
			logger.debug("reconGroup {} took {} ms", group.getName(), (System.currentTimeMillis() - a)); a = System.currentTimeMillis();
		}
		
		ldapWorker.closeConnections();
	}
	
	@Override
	public void deleteGroup(GroupEntity group, ServiceEntity service, Auditor auditor)
			 throws RegisterException {
		logger.debug("Delete Ldap Group for group {} and Service {}", group.getName(), service.getName());
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());

		ldapWorker.deleteGroup(constructGroupName(group));		
		
		ldapWorker.closeConnections();
		
	}
}
