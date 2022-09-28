package edu.kit.scc.webreg.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.SshPubKeyApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.service.AuthorizationService;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;

@Stateless
public class AuthorizationServiceImpl implements AuthorizationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private GroupService groupService;
	
	@Inject
	private ProjectService projectService;
	
	@Inject
	private KnowledgeSessionSingleton knowledgeSessionSingleton;
	
	@Inject
	private IdentityDao identityDao;

	@Inject
	private RoleDao roleDao;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private ServiceDao serviceDao;
	
    @Inject
    private ApplicationConfig appConfig;

	@Override
	public List<RegistryEntity> loadAll(SessionManager sessionManager, Long identityId, HttpServletRequest request) {
		IdentityEntity identity = identityDao.findById(identityId);

    	Long rolesTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_rolesTimeout") != null)
    		rolesTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_rolesTimeout"));
    	else 
    		rolesTimeout = 1 * 60 * 1000L;

    	Long groupsTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_groupsTimeout") != null)
    		groupsTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_groupsTimeout"));
    	else 
    		groupsTimeout = 1 * 60 * 1000L;

    	Long projectsTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_projectsTimeout") != null)
    		projectsTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_projectsTimeout"));
    	else 
    		projectsTimeout = 1 * 60 * 1000L;

    	Long unregisteredServiceTimeout;
    	if (appConfig.getConfigValue("AuthorizationBean_unregisteredServiceTimeout") != null)
    		unregisteredServiceTimeout = Long.parseLong(appConfig.getConfigValue("AuthorizationBean_unregisteredServiceTimeout"));
    	else 
    		unregisteredServiceTimeout = 1 * 60 * 1000L;
    	
    	long start, end;
    	
    	if (sessionManager.getGroupSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getGroupSetCreated()) > groupsTimeout) {
	    	start = System.currentTimeMillis();

	    	sessionManager.clearGroups();
	    	
	    	for (UserEntity user : identity.getUsers()) {
		    	Set<GroupEntity> groupList = groupService.findByUserWithChildren(user);
		    	
	    		sessionManager.getGroups().addAll(groupList);
	    		for (GroupEntity g : groupList) {
	    			sessionManager.getGroupNames().add(g.getName());
	    		}
	    	}
	    	sessionManager.setGroupSetCreated(System.currentTimeMillis());

	    	end = System.currentTimeMillis();
	    	logger.trace("groups loading took {} ms", (end-start));
    	}

    	if (sessionManager.getProjectSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getProjectSetCreated()) > projectsTimeout) {
	    	start = System.currentTimeMillis();

	    	sessionManager.clearProjects();
	    	
	    	List<ProjectMembershipEntity> projectList = projectService.findByIdentity(identity);
		    	
    		sessionManager.getProjects().addAll(projectList);
	    	sessionManager.setGroupSetCreated(System.currentTimeMillis());

	    	end = System.currentTimeMillis();
	    	logger.trace("projects loading took {} ms", (end-start));
    	}

    	if (sessionManager.getRoleSetCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getRoleSetCreated()) > rolesTimeout) {
	    	start = System.currentTimeMillis();

	    	sessionManager.clearRoleList();
	    	
	    	for (UserEntity user : identity.getUsers()) {
		    	Set<RoleEntity> roles = new HashSet<RoleEntity>(roleDao.findByUser(user));
		    	List<RoleEntity> rolesForGroupList = roleDao.findByGroups(sessionManager.getGroups());
		    	roles.addAll(rolesForGroupList);
	
		    	for (RoleEntity role : roles) {
		    		sessionManager.addRole(role);
		    		if (role instanceof AdminRoleEntity) {
		    			for (ServiceEntity s : serviceDao.findByAdminRole(role))
		    				sessionManager.getServiceAdminList().add(s);
		    			for (ServiceEntity s : serviceDao.findByHotlineRole(role))
		    				sessionManager.getServiceHotlineList().add(s);
		    		}
		    		else if (role instanceof ApproverRoleEntity) {
		    			for (ServiceEntity s : serviceDao.findByApproverRole(role))
		    				sessionManager.getServiceApproverList().add(s);
		    		}
		    		else if (role instanceof SshPubKeyApproverRoleEntity) {
		    			for (ServiceEntity s : serviceDao.findBySshPubKeyApproverRole(role))
		    				sessionManager.getServiceSshPubKeyApproverList().add(s);
		    		}
		    		else if (role instanceof GroupAdminRoleEntity) {
		    			for (ServiceEntity s : serviceDao.findByGroupAdminRole(role))
		    				sessionManager.getServiceGroupAdminList().add(s);
		    		}
		    		else if (role instanceof ProjectAdminRoleEntity) {
		    			for (ServiceEntity s : serviceDao.findByProjectAdminRole(role))
		    				sessionManager.getServiceProjectAdminList().add(s);
		    		}
		    	}
	    	}
	    	
	    	end = System.currentTimeMillis();
	    	logger.trace("Role loading took {} ms", (end-start));
	    	
	    	sessionManager.setRoleSetCreated(System.currentTimeMillis());
    	}

    	start = System.currentTimeMillis();
    	List<RegistryEntity> userRegistryList = registryDao.findByIdentityAndNotStatusAndNotHidden(
    			identity, RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
    	end = System.currentTimeMillis();
    	logger.trace("registered servs loading took {} ms", (end-start));
    	
    	if (sessionManager.getUnregisteredServiceCreated() == null || 
    			(System.currentTimeMillis() - sessionManager.getUnregisteredServiceCreated()) > unregisteredServiceTimeout) {
    		
    		List<ServiceEntity> unregisteredServiceList = serviceDao.findAllPublishedWithServiceProps();
	    	
	    	for (RegistryEntity registry : userRegistryList) {
	    		unregisteredServiceList.remove(registry.getService());
	    	}
	
	    	if (appConfig.getConfigValue("service_filter_rule") != null) {
	    		String serviceFilterRule = appConfig.getConfigValue("service_filter_rule");
				logger.debug("Checking service filter rule {}", serviceFilterRule);
		    	start = System.currentTimeMillis();
	
		    	List<ServiceEntity> tempList = new ArrayList<ServiceEntity>();
		    	
		    	tempList.addAll(knowledgeSessionSingleton.checkServiceFilterRule(
			    			serviceFilterRule, identity, unregisteredServiceList,
			    			sessionManager.getGroups(), sessionManager.getRoles(), request));
		    	
		    	unregisteredServiceList = tempList;
		    	
		    	end = System.currentTimeMillis();
		    	logger.debug("Rule processing took {} ms", end - start);
	
	    	}
	    	else {
		    	List<ServiceEntity> tempList = new ArrayList<ServiceEntity>();
		    	for (ServiceEntity s : unregisteredServiceList) {
		    		Map<String, String> serviceProps = s.getServiceProps();
		
		    		for (UserEntity user : identity.getUsers()) {
			    		if (serviceProps.containsKey("idp_filter")) {
			    			String idpFilter = serviceProps.get("idp_filter");
			    			if ((idpFilter != null) && (user instanceof SamlUserEntity) &&
			    					(idpFilter.contains(((SamlUserEntity) user).getIdp().getEntityId())))
				    			tempList.add(s);
			    		}		    			
			    		else if (s.getServiceProps().containsKey("group_filter")) {
			    			String groupFilter = serviceProps.get("group_filter");
			    			if (groupFilter != null &&
			    					(sessionManager.getGroupNames().contains(groupFilter)))
				    			tempList.add(s);
			    		}
			    		else if (s.getServiceProps().containsKey("entitlement_filter")) {
			    			String entitlementFilter = serviceProps.get("entitlement_filter");
			    			String entitlement = user.getAttributeStore().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.7");
			    			if (entitlementFilter != null && entitlement != null &&
			    					( entitlement.matches(entitlementFilter)))
				    			tempList.add(s);
			    		}
			    		else {
			    			tempList.add(s);
			    		}
			    	}
		    	}
		    	unregisteredServiceList = tempList;
	    	}
	    	
	    	sessionManager.setUnregisteredServiceList(unregisteredServiceList);
	    	sessionManager.setUnregisteredServiceCreated(System.currentTimeMillis());    
    	}
    	
    	return userRegistryList;
	}
}
