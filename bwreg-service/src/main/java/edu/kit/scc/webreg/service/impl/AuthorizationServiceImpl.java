package edu.kit.scc.webreg.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
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
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

@Stateless
public class AuthorizationServiceImpl implements AuthorizationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private GroupDao groupDao;

	@Inject
	private ProjectDao projectDao;

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
		IdentityEntity identity = identityDao.fetch(identityId);

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

		Long unregisteredServiceTimeout;
		if (appConfig.getConfigValue("AuthorizationBean_unregisteredServiceTimeout") != null)
			unregisteredServiceTimeout = Long
					.parseLong(appConfig.getConfigValue("AuthorizationBean_unregisteredServiceTimeout"));
		else
			unregisteredServiceTimeout = 1 * 60 * 1000L;

		long start, end;

		if (sessionManager.getGroupSetCreated() == null
				|| (System.currentTimeMillis() - sessionManager.getGroupSetCreated()) > groupsTimeout) {
			start = System.currentTimeMillis();

			sessionManager.clearGroups();

			for (UserEntity user : identity.getUsers()) {
				Set<GroupEntity> groupList = groupDao.findByUserWithChildren(user);

				groupList.stream().forEach(group -> { 
					sessionManager.getGroupIds().add(group.getId());
					sessionManager.getGroupNames().add(group.getName());
				});
			}
			sessionManager.setGroupSetCreated(System.currentTimeMillis());

			end = System.currentTimeMillis();
			logger.trace("groups loading took {} ms", (end - start));
		}

		if (sessionManager.getRoleSetCreated() == null
				|| (System.currentTimeMillis() - sessionManager.getRoleSetCreated()) > rolesTimeout) {
			start = System.currentTimeMillis();

			sessionManager.clearRoleList();

			for (UserEntity user : identity.getUsers()) {
				Set<RoleEntity> roles = new HashSet<RoleEntity>(roleDao.findByUser(user));
				List<RoleEntity> rolesForGroupList = roleDao.findByGroups(sessionManager.getGroupIds());
				roles.addAll(rolesForGroupList);

				for (RoleEntity role : roles) {
					sessionManager.addRole(role);
					if (role instanceof AdminRoleEntity) {
						for (ServiceEntity s : serviceDao.findByAdminRole((AdminRoleEntity) role))
							sessionManager.getServiceAdminList().add(s.getId());
						for (ServiceEntity s : serviceDao.findByHotlineRole((AdminRoleEntity) role))
							sessionManager.getServiceHotlineList().add(s.getId());
					} else if (role instanceof ApproverRoleEntity) {
						for (ServiceEntity s : serviceDao.findByApproverRole((ApproverRoleEntity) role))
							sessionManager.getServiceApproverList().add(s.getId());
					} else if (role instanceof SshPubKeyApproverRoleEntity) {
						for (ServiceEntity s : serviceDao
								.findBySshPubKeyApproverRole((SshPubKeyApproverRoleEntity) role))
							sessionManager.getServiceSshPubKeyApproverList().add(s.getId());
					} else if (role instanceof GroupAdminRoleEntity) {
						for (ServiceEntity s : serviceDao.findByGroupAdminRole((GroupAdminRoleEntity) role))
							sessionManager.getServiceGroupAdminList().add(s.getId());
					} else if (role instanceof ProjectAdminRoleEntity) {
						for (ServiceEntity s : serviceDao.findByProjectAdminRole((ProjectAdminRoleEntity) role))
							sessionManager.getServiceProjectAdminList().add(s.getId());
					}
				}
			}

			end = System.currentTimeMillis();
			logger.trace("Role loading took {} ms", (end - start));

			sessionManager.setRoleSetCreated(System.currentTimeMillis());
		}

		start = System.currentTimeMillis();
		List<RegistryEntity> userRegistryList = registryDao.findByIdentityAndNotStatusAndNotHidden(identity,
				RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
		end = System.currentTimeMillis();
		logger.trace("registered servs loading took {} ms", (end - start));

		if (sessionManager.getUnregisteredServiceCreated() == null || (System.currentTimeMillis()
				- sessionManager.getUnregisteredServiceCreated()) > unregisteredServiceTimeout) {

			List<ServiceEntity> unregisteredServiceList = serviceDao.findAllPublishedWithServiceProps();

			for (RegistryEntity registry : userRegistryList) {
				unregisteredServiceList.remove(registry.getService());
			}

			if (appConfig.getConfigValue("service_filter_rule") != null) {
				String serviceFilterRule = appConfig.getConfigValue("service_filter_rule");
				logger.debug("Checking service filter rule {}", serviceFilterRule);
				start = System.currentTimeMillis();

				List<ServiceEntity> tempList = new ArrayList<ServiceEntity>();

				List<RoleEntity> roleList = roleDao.fetchAll(new ArrayList<>(sessionManager.getRoleIds()));
				List<GroupEntity> groupList = groupDao.fetchAll(new ArrayList<>(sessionManager.getGroupIds()));
				List<ProjectMembershipEntity> projectList = projectDao.findByIdentity(identity);
				
				tempList.addAll(knowledgeSessionSingleton.checkServiceFilterRule(serviceFilterRule, identity,
						unregisteredServiceList, new HashSet<>(groupList), new HashSet<>(roleList), new HashSet<>(projectList)));

				unregisteredServiceList = tempList;

				end = System.currentTimeMillis();
				logger.debug("Rule processing took {} ms", end - start);

			} else {
				List<ServiceEntity> tempList = new ArrayList<ServiceEntity>();
				for (ServiceEntity s : unregisteredServiceList) {
					Map<String, String> serviceProps = s.getServiceProps();

					for (UserEntity user : identity.getUsers()) {
						if (serviceProps.containsKey("idp_filter")) {
							String idpFilter = serviceProps.get("idp_filter");
							if ((idpFilter != null) && (user instanceof SamlUserEntity)
									&& (idpFilter.contains(((SamlUserEntity) user).getIdp().getEntityId())))
								tempList.add(s);
						} else if (s.getServiceProps().containsKey("group_filter")) {
							String groupFilter = serviceProps.get("group_filter");
							if (groupFilter != null && (sessionManager.getGroupNames().contains(groupFilter)))
								tempList.add(s);
						} else if (s.getServiceProps().containsKey("entitlement_filter")) {
							String entitlementFilter = serviceProps.get("entitlement_filter");
							String entitlement = user.getAttributeStore().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.7");
							if (entitlementFilter != null && entitlement != null
									&& (entitlement.matches(entitlementFilter)))
								tempList.add(s);
						} else {
							tempList.add(s);
						}
					}
				}
				unregisteredServiceList = tempList;
			}

			sessionManager.setUnregisteredServiceList(unregisteredServiceList.stream().map(service -> service.getId()).toList());
			sessionManager.setUnregisteredServiceCreated(System.currentTimeMillis());
		}

		return userRegistryList;
	}
}
