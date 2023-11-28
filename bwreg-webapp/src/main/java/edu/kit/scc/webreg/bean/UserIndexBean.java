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
package edu.kit.scc.webreg.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;

@Named("userIndexBean")
@RequestScoped
public class UserIndexBean {

	private Map<ServiceEntity, String> serviceAccessMap;

	@Inject
	private Logger logger;

	@Inject
	private ServiceService serviceService;

	@Inject
	private RegistryService registryService;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private IdentityService identityService;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;

	@PostConstruct
	public void init() {
		IdentityEntity identity = getIdentity();
		List<RegistryEntity> userRegistryList = getUserRegistryList();

		serviceAccessMap = new HashMap<ServiceEntity, String>(userRegistryList.size());

		long start = System.currentTimeMillis();
		checkServiceAccess(userRegistryList, identity);
		long end = System.currentTimeMillis();
		logger.debug("Rule processing took {} ms", end - start);
	}

	public String getServiceAccessStatus(ServiceEntity service) {
		return serviceAccessMap.get(service);
	}

	public List<ServiceEntity> getAllServiceList() {
		return serviceService.findAllPublishedWithServiceProps();
	}

	public List<RegistryEntity> getUserRegistryList() {
		return registryService.findByIdentityAndNotStatusAndNotHidden(getIdentity(), RegistryStatus.DELETED,
				RegistryStatus.DEPROVISIONED, RegistryStatus.PENDING);
	}

	public List<RegistryEntity> getPendingRegistryList() {
		return registryService.findByIdentityAndStatus(getIdentity(), RegistryStatus.PENDING);
	}

	private void checkServiceAccess(List<RegistryEntity> userRegistryList, IdentityEntity identity) {

		Map<RegistryEntity, List<Object>> objectMap = new HashMap<RegistryEntity, List<Object>>();
		
		for (RegistryEntity registry : userRegistryList) {
			List<Object> objectList = knowledgeSessionService.checkServiceAccessRule(registry.getUser(),
							registry.getService(), registry, "identity-" + sessionManager.getIdentityId(), false);
			objectMap.put(registry, objectList);
		}

		for (Entry<RegistryEntity, List<Object>> entry : objectMap.entrySet()) {
			RegistryEntity registry = entry.getKey();
			List<Object> objectList = entry.getValue();

			StringBuffer sb = new StringBuffer();
			for (Object o : objectList) {
				if (o instanceof OverrideAccess) {
					objectList.clear();
					sb.setLength(0);
					logger.debug("Removing requirements due to OverrideAccess");
					break;
				} else if (o instanceof UnauthorizedUser) {
					String s = ((UnauthorizedUser) o).getMessage();
					sb.append(s);
				}
			}

			if (sb.length() > 0)
				serviceAccessMap.put(registry.getService(), sb.toString());
		}
	}

	public IdentityEntity getIdentity() {
		return identityService.fetch(sessionManager.getIdentityId());
	}

}
