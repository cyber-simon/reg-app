package edu.kit.scc.webreg.hook;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.GroupServiceHook;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.LdapConfig.SearchScope;
import edu.vt.middleware.ldap.SearchFilter;
import edu.vt.middleware.ldap.handler.ConnectionHandler.ConnectionStrategy;

public class LdapUidNumberGroupHook implements GroupServiceHook {

	private Logger logger = LoggerFactory.getLogger(LdapUidNumberGroupHook.class);

	private AttributeMapHelper attrHelper = new AttributeMapHelper();
	
	private ApplicationConfig appConfig;
	
	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}
	
	@Override
	public HomeOrgGroupEntity preUpdateUserPrimaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, HomeOrgGroupEntity group, UserEntity genericUser,
			Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) throws UserUpdateException {

		if (! (genericUser instanceof SamlUserEntity)) {
			throw new UserUpdateException("Hook is intended for SamlUserEntites only");
		}

		SamlUserEntity user = (SamlUserEntity) genericUser;
		
		logger.info("LDAP-User Detected. Taking Primary Group from assertion");
		
		if (attributeMap.get("urn:oid:1.3.6.1.1.1.1.1") == null) {
			 if (user.getPrimaryGroup() == null) {
				 throw new UserUpdateException("GID Number Attribut ist nicht gesetzt und User hat keine primäre Gruppe");
			 }
			 else {
				 logger.warn("User {} hat keine Gid in SAML Assertion, aber schon eine primäre Gruppe ({}).", user.getEppn(), user.getPrimaryGroup().getName());
				 return dao.findById(user.getPrimaryGroup().getId());
			 }
		}
		
		int gid = Integer.parseInt(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:1.3.6.1.1.1.1.1"));
		group = dao.findByGidNumber(gid);
		
		Ldap ldap = getLdapConnection();

		String groupNameFromLdap = null;
		
		try {
			// Search for groupName in LDAP
			Iterator<SearchResult> iterator = ldap.search(new SearchFilter("gidNumber="+gid), new String[] {"cn"});
			if (iterator.hasNext()) {
				SearchResult sr = iterator.next();
				if (sr.getAttributes() != null &&
						sr.getAttributes().get("cn") != null) {
					Object o = sr.getAttributes().get("cn").get();
					if (o instanceof String) {
						groupNameFromLdap = (String) o;
						
						// Lowercase groupname
						groupNameFromLdap = groupNameFromLdap.toLowerCase();

						// Strip '-users-idm'
						groupNameFromLdap = groupNameFromLdap.replaceAll("-users-idm$", "");
						groupNameFromLdap = Normalizer.normalize(groupNameFromLdap, Normalizer.Form.NFD);
						groupNameFromLdap = groupNameFromLdap.replaceAll("[^a-z0-9\\-_]", "");
						
						logger.debug("GidNumber {} from LDAP results in group: {}", gid, groupNameFromLdap);
					}
					else
						logger.warn("cn not of type String for group {}", group);
				}
				else
					logger.warn("No SearchResult Attributes available from ldap for group {}", group);
			}
			else
				logger.warn("No SearchResults available from ldap for group {}", group);
		} catch (NamingException e) {
			logger.warn("Problem with LDAP: {}", e.getMessage());
		}
		
		if (group == null) {
			if (groupNameFromLdap == null) {
				logger.warn("Group with GID {} not available in LDAP. Creating one with GID as name", gid);
				group = dao.createNew();
				group.setUsers(new HashSet<UserGroupEntity>());
				group.setParents(new HashSet<GroupEntity>());
				group.setGidNumber(gid);
				group.setName("noname_" + gid);
				group.setPrefix("ka");
				group.setIdp(user.getIdp());
				group = (HomeOrgGroupEntity) groupDao.persistWithServiceFlags(group);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "idpEntityId", "" + user.getIdp().getEntityId(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "name", group.getName(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "prefix", group.getPrefix(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "gidNumber", "" + group.getGidNumber(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "CREATE GROUP (LDAP)", null, "Group created", AuditStatus.SUCCESS);
				
				changedGroups.add(group);
			}
			else {
				logger.info("Group with GID {} not found, creating (Name: {})", gid, groupNameFromLdap);
				group = dao.createNew();
				group.setUsers(new HashSet<UserGroupEntity>());
				group.setParents(new HashSet<GroupEntity>());
				group.setGidNumber(gid);
				group.setName(groupNameFromLdap);
				group.setPrefix("ka");
				group.setIdp(user.getIdp());
				group = (HomeOrgGroupEntity) groupDao.persistWithServiceFlags(group);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "idpEntityId", "" + user.getIdp().getEntityId(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "name", group.getName(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "prefix", group.getPrefix(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "SET FIELD (LDAP)", "gidNumber", "" + group.getGidNumber(), AuditStatus.SUCCESS);
				auditor.logAction(group.getName(), "CREATE GROUP (LDAP)", null, "Group created", AuditStatus.SUCCESS);

				changedGroups.add(group);
			}
		}
		else {
			if (groupNameFromLdap == null) {
				logger.warn("Cannot compare Group with GID {}. No Group available in LDAP. Leaving it as group {}", gid, group.getName());
			}
			else {
				if (! group.getName().equals(groupNameFromLdap)){
					logger.warn("Updating Groupname {} to {}", group.getName(), groupNameFromLdap);
					group.setName(groupNameFromLdap);
					group = dao.persist(group);
					auditor.logAction(group.getName(), "SET FIELD (LDAP)", "name", "" + group.getName(), AuditStatus.SUCCESS);
				}
				else {
					logger.warn("Groupname {} stayed the same. Ain't not doing nothing at all.", group.getName());
				}
			}
		}
		
		return group;
	}

	@Override
	public HomeOrgGroupEntity postUpdateUserPrimaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, HomeOrgGroupEntity group, UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) throws UserUpdateException {
		return group;
	}

	@Override
	public void preUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, UserEntity genericUser,
			Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) throws UserUpdateException {

		if (! (genericUser instanceof SamlUserEntity)) {
			throw new UserUpdateException("Hook is intended for SamlUserEntites only");
		}

		SamlUserEntity user = (SamlUserEntity) genericUser;
		
		logger.info("LDAP-User Detected. Taking Secondary Groups from assertion");
		
		if (attributeMap.get("memberOf") == null) {
			logger.info("No memberOf is set. Skipping secondary groups");
			return;
		}
		
		List<String> tempGroupList = attrHelper.attributeListToStringList(attributeMap, "memberOf");
		List<String> groupList = new ArrayList<String>(tempGroupList.size());

		//
		// Format Group Names from Assertion
		//
		logger.debug("Formatting group names");
		for (String group : tempGroupList) {
				// Lowercase groupname
				group = group.toLowerCase();

				// Strip '-users-idm'
				group = group.replaceAll("-users-idm$", "");

				group = Normalizer.normalize(group, Normalizer.Form.NFD);
				group = group.replaceAll("[^a-z0-9\\-_]", "");

				groupList.add(group);
		}
		
		if (user.getGroups() == null)
			user.setGroups(new HashSet<UserGroupEntity>());

		Set<GroupEntity> groupsFromAssertion = new HashSet<GroupEntity>();
		
		Ldap ldap = getLdapConnection();
		
		logger.debug("Looking up groups from database");
		Map<String, HomeOrgGroupEntity> dbGroupMap = new HashMap<String, HomeOrgGroupEntity>();
		logger.debug("Indexing groups from database");
		for (HomeOrgGroupEntity dbGroup : dao.findByNameListAndPrefix(groupList, "ka")) {
			dbGroupMap.put(dbGroup.getName(), dbGroup);
		}
		
		for (String group : groupList) {
			if (group != null && (!group.equals(""))) {

				logger.debug("Analyzing group {}", group);
				HomeOrgGroupEntity groupEntity = dbGroupMap.get(group);
				
				try {
					if (groupEntity == null) {
						// Check for gidNumber from LDAP
						Iterator<SearchResult> iterator = ldap.search(new SearchFilter("cn="+group), new String[] {"gidNumber"});
						if (iterator.hasNext()) {
							SearchResult sr = iterator.next();
							if (sr.getAttributes() != null &&
									sr.getAttributes().get("gidNumber") != null) {
								Object o = sr.getAttributes().get("gidNumber").get();
								if (o instanceof String) {
									Integer gidNumber = Integer.parseInt((String) o);
									
									groupEntity = dao.findByGidNumber(gidNumber);
									
									if (groupEntity == null) {
										logger.info("Creating group {} with gidNumber {}", group, gidNumber);
										groupEntity = dao.createNew();
										groupEntity.setUsers(new HashSet<UserGroupEntity>());
										groupEntity.setName(group);
										groupEntity.setPrefix("ka");
										groupEntity.setGidNumber(gidNumber);
										groupEntity.setIdp(user.getIdp());
										groupEntity = (HomeOrgGroupEntity) groupDao.persistWithServiceFlags(groupEntity);
										auditor.logAction(groupEntity.getName(), "SET FIELD (LDAP)", "idpEntityId", "" + user.getIdp().getEntityId(), AuditStatus.SUCCESS);
										auditor.logAction(groupEntity.getName(), "SET FIELD (LDAP)", "name", groupEntity.getName(), AuditStatus.SUCCESS);
										auditor.logAction(groupEntity.getName(), "SET FIELD (LDAP)", "prefix", groupEntity.getPrefix(), AuditStatus.SUCCESS);
										auditor.logAction(groupEntity.getName(), "SET FIELD (LDAP)", "gidNumber", "" + groupEntity.getGidNumber(), AuditStatus.SUCCESS);
										
										auditor.logAction(groupEntity.getName(), "CREATE GROUP (LDAP)", null, "Group created", AuditStatus.SUCCESS);
										
										changedGroups.add(groupEntity);
									}
									else {
										logger.info("Renaming Group {} to name -> {}", groupEntity.getName(), group);
										auditor.logAction(groupEntity.getName(), "RENAME GROUP (LDAP)", group, groupEntity.getName() + " -> " + group, AuditStatus.SUCCESS);
										groupEntity.setName(group);
										groupEntity = dao.persist(groupEntity);
										auditor.logAction(groupEntity.getName(), "SET FIELD (LDAP)", "name", groupEntity.getName(), AuditStatus.SUCCESS);
										
										changedGroups.add(groupEntity);
									}
								}
								else
									logger.warn("GidNumber not of type String for group {}", group);
							}
							else
								logger.warn("No SearchResult Attributes available from ldap for group {}", group);
						}
						else
							logger.warn("No SearchResults available from kitldap for group {}", group);
					}
				
					if (groupEntity != null) {
						groupsFromAssertion.add(groupEntity);
						
						if (! groupDao.isUserInGroup(user, groupEntity)) {
							logger.debug("Adding user {} to group {}", user.getEppn(), groupEntity.getName());
							groupDao.addUserToGroup(user, groupEntity);
							changedGroups.remove(groupEntity);
							auditor.logAction(user.getEppn(), "ADD TO GROUP (LDAP)", groupEntity.getName(), null, AuditStatus.SUCCESS);
							
							changedGroups.add(groupEntity);
						}
					}
				} catch (NumberFormatException e) {
					logger.warn("GidNumber has a bad number format: {}", e.getMessage());
				} catch (NamingException e) {
					logger.warn("Problem with LDAP: {}", e.getMessage());
				}
			}
		}
		
		
		Set<GroupEntity> groupsToRemove = new HashSet<GroupEntity>(groupDao.findByUser(user));
		groupsToRemove.removeAll(groupsFromAssertion);

		for (GroupEntity removeGroup : groupsToRemove) {
			if (removeGroup instanceof HomeOrgGroupEntity) {
				if (! removeGroup.equals(user.getPrimaryGroup())) {
					logger.debug("Removing user {} from group {}", user.getEppn(), removeGroup.getName());
					groupDao.removeUserGromGroup(user, removeGroup);
					
					auditor.logAction(user.getEppn(), "REMOVE FROM GROUP (LDAP)", removeGroup.getName(), null, AuditStatus.SUCCESS);
	
					changedGroups.add(removeGroup);
				}
			}
			else {
				logger.debug("Group {} of type {}. Doing nothing.", removeGroup.getName(), removeGroup.getClass().getSimpleName());
			}
		}
	
		/*
		 * Add Primary group to secondary as well
		 */
		if (user.getPrimaryGroup() != null && (! groupDao.isUserInGroup(user, user.getPrimaryGroup()))) {
			logger.debug("Adding user {} to his primary group {} as secondary", user.getEppn(), user.getPrimaryGroup().getName());
			groupDao.addUserToGroup(user, user.getPrimaryGroup());
			changedGroups.add(user.getPrimaryGroup());
		}
		
		ldap.close();

	}

	@Override
	public void postUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) throws UserUpdateException {
		logger.debug("Empty postUpdateUserSecondaryGroupFromAttribute called");
	}

	@Override
	public boolean isPrimaryResponsible(UserEntity genericUser,
			Map<String, List<Object>> attributeMap) {
		
		if (genericUser instanceof SamlUserEntity) {
			SamlUserEntity user = (SamlUserEntity) genericUser;
			
			if (appConfig != null) {
				String entityIdsConfig = "";
				if (appConfig.getConfigValue("LdapUidNumberGroupHook_entityIds") != null) {
					entityIdsConfig = appConfig.getConfigValue("LdapUidNumberGroupHook_entityIds");
				}
				String[] entityIds = entityIdsConfig.split(" ");
				for (String entityId : entityIds) {
					if (user.getIdp().getEntityId().equals(entityId.trim()))
						return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public boolean isPrimaryCompleteOverride() {
		return true;
	}

	@Override
	public boolean isSecondaryResponsible(UserEntity genericUser,
			Map<String, List<Object>> attributeMap) {
		if (genericUser instanceof SamlUserEntity) {
			SamlUserEntity user = (SamlUserEntity) genericUser;
			
			if (appConfig != null) {
				String entityIdsConfig = "";
				if (appConfig.getConfigValue("LdapUidNumberGroupHook_entityIds") != null) {
					entityIdsConfig = appConfig.getConfigValue("LdapUidNumberGroupHook_entityIds");
				}
				String[] entityIds = entityIdsConfig.split(" ");
				for (String entityId : entityIds) {
					if (user.getIdp().getEntityId().equals(entityId.trim()))
						return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public boolean isSecondaryCompleteOverride() {
		return true;
	}
	
	protected Ldap getLdapConnection() {
		String ldapUrl = "ldap://localhost:636";
		String ldapBase = "ou=Groups,dc=example,dc=com";
		String bindDn = "uid=user1,ou=ProxyUser,dc=example,dc=com";
		String bindPassword = "asdf";
		String ssl = "";
		String connectionStrategy = "RANDOM";
		String scope = "SUBTREE";
		int timeout = 5000;
		
		if (appConfig != null) {
			if (appConfig.getConfigValue("LdapUidNumberGroupHook_ldapUrl") != null) {
				ldapUrl = appConfig.getConfigValue("LdapUidNumberGroupHook_ldapUrl");
			}

			if (appConfig.getConfigValue("LdapUidNumberGroupHook_ldapBase") != null) {
				ldapBase = appConfig.getConfigValue("LdapUidNumberGroupHook_ldapBase");
			}

			if (appConfig.getConfigValue("LdapUidNumberGroupHook_bindDn") != null) {
				bindDn = appConfig.getConfigValue("LdapUidNumberGroupHook_bindDn");
			}
			
			if (appConfig.getConfigValue("LdapUidNumberGroupHook_bindPassword") != null) {
				bindPassword = appConfig.getConfigValue("LdapUidNumberGroupHook_bindPassword");
			}
			
			if (appConfig.getConfigValue("LdapUidNumberGroupHook_ssl") != null) {
				ssl = appConfig.getConfigValue("LdapUidNumberGroupHook_ssl");
			}
			
			if (appConfig.getConfigValue("LdapUidNumberGroupHook_timeout") != null) {
				timeout = Integer.parseInt(appConfig.getConfigValue("LdapUidNumberGroupHook_ssl"));
			}

			if (appConfig.getConfigValue("LdapUidNumberGroupHook_connectionStrategy") != null) {
				connectionStrategy = appConfig.getConfigValue("LdapUidNumberGroupHook_connectionStrategy").toUpperCase(Locale.US);
			}
						
			if (appConfig.getConfigValue("LdapUidNumberGroupHook_scope") != null) {
				scope = appConfig.getConfigValue("LdapUidNumberGroupHook_scope").toUpperCase(Locale.US);
			}
						
		}
		
		LdapConfig config = new LdapConfig(ldapUrl, ldapBase);
		config.setBindDn(bindDn);
		config.setBindCredential(bindPassword);

		config.setSearchScope(SearchScope.valueOf(scope));
		config.getConnectionHandler().setConnectionStrategy(ConnectionStrategy.valueOf(connectionStrategy));
		
		if ("ssl".equals(ssl))
			config.setSsl(true);
		else if ("tls".equals(ssl))
			config.setTls(true);
			
		config.setTimeout(timeout);
		
		Ldap ldap = new Ldap(config);
		return ldap;
	}
}
