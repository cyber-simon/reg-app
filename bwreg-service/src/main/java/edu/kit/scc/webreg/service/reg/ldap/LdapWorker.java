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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.vt.middleware.ldap.AttributesFactory;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.Ldap.AttributeModification;
import edu.vt.middleware.ldap.SearchFilter;

public class LdapWorker {

	private static Logger logger = LoggerFactory.getLogger(LdapWorker.class);
	
	private String ldapUserBase;
	private String ldapGroupBase;
    private String ldapUserObjectclasses;
    private String ldapGroupObjectclasses;
	
	private String ldapGroupType;
	
	private boolean sambaEnabled;
	private String sidPrefix;
	
	private LdapConnectionManager connectionManager;
	
	private Auditor auditor;

	public LdapWorker(PropertyReader prop, Auditor auditor, boolean sambaEnabled) throws RegisterException {
		this.auditor = auditor;
		this.sambaEnabled = sambaEnabled;
		
		try {
			connectionManager = new LdapConnectionManager(prop);
			ldapUserBase = prop.readProp("ldap_user_base");
			ldapGroupBase = prop.readProp("ldap_group_base");
                        
			ldapUserObjectclasses = prop.readPropOrNull("ldap_user_objectclass");
			ldapGroupObjectclasses = prop.readPropOrNull("ldap_group_objectclass");

			ldapGroupType = prop.readPropOrNull("group_type");

			if (sambaEnabled)
				sidPrefix = prop.readProp("sid_prefix");
			

		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		}		
	}

	public void deleteUser(String uid) {

		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				ldap.delete("uid=" + uid + "," + ldapUserBase);
				logger.info("Deleted User {} from ldap {}", 
						new Object[] {uid, ldapUserBase});
				auditor.logAction("", "DELETE LDAP USER", uid, "User deleted in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Delete User {} from ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "DELETE LDAP USER", uid, "User deletion failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}
	}
	
	public void reconGroup(String cn, String gidNumber, Set<String> memberUids) {
		String dn = "cn=" + cn + "," + ldapGroupBase;
		
		if (memberUids.size() == 0) {
			deleteGroup(cn);
			return;
		}

		createGroup(cn, gidNumber);
		
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				Set<String> oldMemberUids = new HashSet<String>();

				String memberAttribute;
				if (ldapGroupType != null && ldapGroupType.equals("member")) {
					memberAttribute = "member";
				}
				else {
					memberAttribute = "memberUid";
				}

				Attributes attrs = ldap.getAttributes(dn, new String[]{memberAttribute});
				Attribute attr = attrs.get(memberAttribute);

				if (attr != null) {
					for (int i=0; i<attr.size(); i++) {
						String memberUid = (String) attr.get(i);
						oldMemberUids.add(memberUid);
					}
				}
				
				Set<String> addMemberUids = new HashSet<String>(memberUids);
				addMemberUids.removeAll(oldMemberUids);

				Set<String> removeMemberUids = new HashSet<String>(oldMemberUids);
				removeMemberUids.removeAll(memberUids);

				for (String memberUid : addMemberUids) {
					logger.info("Adding member {} to group {}", memberUid, cn);
					try {
						ldap.modifyAttributes(dn, AttributeModification.ADD, 
								AttributesFactory.createAttributes(memberAttribute, memberUid));
						auditor.logAction(cn, "ADD LDAP GROUP MEMBER", memberUid, "Added member on " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
					} catch (NamingException e) {
						auditor.logAction(cn, "ADD LDAP GROUP MEMBER", memberUid, "Add member on " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
					}
				}
				
				for (String memberUid : removeMemberUids) {
					logger.info("Removing member {} from group {}", memberUid, cn);
					try {
						ldap.modifyAttributes(dn, AttributeModification.REMOVE, 
								AttributesFactory.createAttributes(memberAttribute, memberUid));
						auditor.logAction(cn, "REMOVE LDAP GROUP MEMBER", memberUid, "Removed member on " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
					} catch (NamingException e) {
						auditor.logAction(cn, "REMOVE LDAP GROUP MEMBER", memberUid, "Remove member on " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
					}
				}

			} catch (NamingException e) {
				if (ldap.getLdapConfig() != null)
					logger.info("Group action failed for connection " + ldap.getLdapConfig().getLdapUrl(), e);
				else
					logger.info("Group action failed, and oh no, ldapConfig is null!", e);
			}			
		}		
	}
	
	public void deleteGroup(String cn) {

		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				ldap.delete("cn=" + cn + "," + ldapGroupBase);
				logger.info("Deleted Group {} from ldap {}", 
						new Object[] {cn, ldapGroupBase});
				auditor.logAction("", "DELETE LDAP GROUP", cn, "Group deleted in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Delete Group {} from ldap {}", 
						new Object[] {cn, ldapUserBase});
				auditor.logAction("", "DELETE LDAP GROUP", cn, "Group deletion failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}
	}

	public void reconUser(String cn, String sn, String givenName, String mail, String uid, String uidNumber, String gidNumber,
			String homeDir, String description) {
		reconUser(cn, sn, givenName, mail, uid, uidNumber, gidNumber, homeDir, description, null);
	}
	
	public void reconUser(String cn, String sn, String givenName, String mail, String uid, String uidNumber, String gidNumber,
			String homeDir, String description, Map<String, String> extraAttributesMap) {
		for (Ldap ldap : connectionManager.getConnections()) {
			List<String> dnList = new ArrayList<String>();

			try {
				Iterator<SearchResult> iterator = ldap.search(new SearchFilter("uidNumber="+uidNumber), new String[] {"uid"});
				while (iterator.hasNext()) {
					SearchResult sr = iterator.next();
					dnList.add(sr.getName());
				}
			} catch (NamingException e) {
				logger.warn("FAILED: Search user {}, uid:{}, gid:{} with ldap {}: {}", 
						new Object[] {uid, uidNumber, gidNumber, 
						ldapUserBase, e.getMessage()});
			}
			
			if (dnList.size() == 0) {
				logger.debug("Account does not exist. Creating...");
				try {
					createUserIntern(ldap, cn, givenName, sn, mail, uid, uidNumber, gidNumber, homeDir, description, extraAttributesMap);
					logger.info("User {},{} with ldap {} successfully created", 
							new Object[] {uid, gidNumber, ldapUserBase});
					auditor.logAction("", "RECON CREATE LDAP USER", uid, "User created in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
				} catch (NamingException ne) {
					logger.warn("FAILED: User {}, uid:{}, gid:{} with ldap {}: {}", 
							new Object[] {uid, uidNumber, gidNumber, 
							ldapUserBase, ne.getMessage()});
					auditor.logAction("", "RECON CREATE LDAP USER", uid, "User creation failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
				}				
			}
			else {
				if (dnList.size() > 1) {
					// More than one entry with uidNumber found. Delete all except one
					for (int i=1; i<dnList.size(); i++) {
						try {
							ldap.delete(dnList.get(i));
							auditor.logAction("", "DELETE LDAP USER", dnList.get(i), "User delete in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
						} catch (NamingException ne) {
							logger.warn("FAILED: Delete user {}, uid:{}, gid:{} with ldap {}: {}", 
									new Object[] {uid, uidNumber, gidNumber, 
									ldapUserBase, ne.getMessage()});
							auditor.logAction("", "DELETE LDAP USER", dnList.get(i), "User delete failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
						}				
					}
				}
				
				try {
					String dn = dnList.get(0);
					Attributes attrs = ldap.getAttributes(dn);

					List<ModificationItem> modList = new ArrayList<ModificationItem>();
					StringBuilder log = new StringBuilder();
					
					compareAttr(attrs, "cn", cn, modList, log);
					compareAttr(attrs, "sn", sn, modList, log);
					compareAttr(attrs, "givenName", givenName, modList, log);
					compareAttr(attrs, "mail", mail, modList, log);
					compareAttr(attrs, "uidNumber", uidNumber, modList, log);
					compareAttr(attrs, "gidNumber", gidNumber, modList, log);
					compareAttr(attrs, "homeDirectory", homeDir, modList, log);
					compareAttr(attrs, "description", description, modList, log);
					
					if (sambaEnabled) {
						addAttrIfNotExists(attrs, "objectClass", "sambaSamAccount", modList);
						compareAttr(attrs, "sambaSID", sidPrefix + (Long.parseLong(uidNumber) * 2L + 1000L), modList, log);					
					}
					
					if (extraAttributesMap != null) {
						for (Entry<String, String> extraAttribute : extraAttributesMap.entrySet()) {
							if (extraAttribute.getKey().startsWith("extra_") && extraAttribute.getKey().length() > 6) {
								compareAttr(attrs, extraAttribute.getKey().substring(6), extraAttribute.getValue(), modList, log);
							}
						}
					}
					
					if (modList.size() == 0) {
						logger.debug("No modification detected");
					}
					else {
						logger.debug("Replacing {} attribute", modList.size());
						ldap.modifyAttributes(dn, modList.toArray(new ModificationItem[modList.size()]));
						if (log.length() > 512) log.setLength(512);
						auditor.logAction("", "RECON LDAP USER", uid, log.toString(), AuditStatus.SUCCESS);
					}
					
					Attribute attr = attrs.get("uid");
					if (attr != null) {
						String oldUid = (String) attr.get();
						if (! uid.equals(oldUid)) {
							ldap.rename(dn, "uid=" + uid + "," + ldapUserBase);
						}
					}
				} catch (NamingException e) {
					logger.warn("FAILED: Recon user {}, uid:{}, gid:{} with ldap {}: {}", 
							new Object[] {uid, uidNumber, gidNumber, 
							ldapUserBase, e.getMessage()});
					auditor.logAction("", "DELETE LDAP USER", uid, "User delete failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
				}
			}
		}		
	}

	@SuppressWarnings("unchecked")
	public void createGroup(String cn, String gidNumber) {
		
		for (Ldap ldap : connectionManager.getConnections()) {
			
			try {
				Iterator<SearchResult> resultIterator = ldap.search(ldapGroupBase,
						  new SearchFilter("(gidNumber=" + gidNumber + ")"), new String[]{"cn", "uid"});
				List<SearchResult> resultList = IteratorUtils.toList(resultIterator);
				
				if (resultList.size() > 1) {
					logger.warn("More than one Ldap Group ({}) with gid {}, delete all except one!", resultList.size(), gidNumber);
					for (int i=1; i<resultList.size(); i++) {
						SearchResult sr = resultList.get(i);
						String dn = sr.getName();
						ldap.delete(dn);
						logger.warn("Deleted group {} (gid {})", dn, gidNumber);
						auditor.logAction("", "DELETE LDAP GROUP", cn, "Group deleted in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
					}
				}
				
				if (resultList.size() > 0) {
					SearchResult sr = resultList.get(0);
					Attribute cnAttr = sr.getAttributes().get("cn");
					String actualCn = (String) cnAttr.get();
					
					if (! cn.equals(actualCn)) {
						logger.warn("Groupname for group {} differs. is {}, should {}. Changing attrs dn, cn", gidNumber, actualCn, cn);
						String dn = sr.getName();
						String newDn = "cn=" + cn + "," + ldapGroupBase;
						ldap.rename(dn, newDn);
						logger.info("Rename Group {} ({}) completed", cn, gidNumber);
						auditor.logAction("", "RENAME LDAP GROUP", cn, "Group renamed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
					}
					
					continue;
				}
				
			} catch (NamingException e) {
				logger.warn("Ldap Error occured", e);
			}
			
			try {
				createGroupIntern(ldap, cn, gidNumber);
				logger.info("Group {},{} with ldap {} successfully created", 
						new Object[] {cn, gidNumber, ldapUserBase});
				auditor.logAction("", "CREATE LDAP GROUP", cn, "Group created in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Group cn:{}, gid:{} with ldap {}: {}", 
						new Object[] {cn, gidNumber, 
						ldapUserBase, e.getMessage()});
				auditor.logAction("", "CREATE LDAP GROUP", cn, "Group creation failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}

		}
	}
	
	public List<String> getPasswords(String uid) {
		List<String> pwList = new ArrayList<String>();
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				String ldapDn = "uid=" + uid + "," + ldapUserBase;
				Attributes attrs = ldap.getAttributes(ldapDn);
				Attribute attr = attrs.get("userPassword");
				if (attr != null) {
					for (int i=0; i<attr.size(); i++) {
						Object attrObject = attr.get(i);
						if (attrObject != null)
							pwList.add(new String((byte[]) attrObject));
					}
				}				
			} catch (NamingException e) {
				logger.warn("FAILED: Getting password for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
			}
		}
		
		return pwList;
	}
	
	public void setPassword(String uid, String password) {
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				String ldapDn = "uid=" + uid + "," + ldapUserBase;
				Attributes attrs = ldap.getAttributes(ldapDn);
				Attribute attr = attrs.get("userPassword");
				if (attr == null) {
					ldap.modifyAttributes(ldapDn, AttributeModification.ADD, 
							AttributesFactory.createAttributes("userPassword", password));
				}
				else {
					ldap.modifyAttributes(ldapDn, AttributeModification.REPLACE, 
							AttributesFactory.createAttributes("userPassword", password));
				}
				logger.info("Setting password for User {} in ldap {}", 
						new Object[] {uid, ldapUserBase});
				auditor.logAction("", "SET PASSWORD LDAP USER", uid, "Set User password in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Setting password for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "SET PASSWORD LDAP USER", uid, "Set User password failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}		
	}
	
	public void setSambaPassword(String uid, String password, UserEntity user) {
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				Attributes attrs = ldap.getAttributes("uid=" + uid + "," + ldapUserBase);

				List<ModificationItem> modList = new ArrayList<ModificationItem>();
				StringBuilder log = new StringBuilder();
				
				addAttrIfNotExists(attrs, "objectClass", "sambaSamAccount", modList);
				compareAttr(attrs, "sambaSID", sidPrefix + (user.getUidNumber().longValue() * 2L + 1000L), modList, log);
				compareAttr(attrs, "sambaNTPassword", password, modList, log);
				compareAttr(attrs, "sambaPasswordHistory", "00000000000000000000000000000000000000000000000000000000", modList, log);
				compareAttr(attrs, "sambaPwdLastSet", "1366812351", modList, log);
				compareAttr(attrs, "sambaAcctFlags", "[U          ]", modList, log);
				
				if (modList.size() == 0) {
					logger.debug("No modification detected");
				}
				else {
					logger.debug("Replacing {} attribute", modList.size());
					ldap.modifyAttributes("uid=" + uid + "," + ldapUserBase, modList.toArray(new ModificationItem[modList.size()]));
				}

				auditor.logAction("", "SET SAMBA PASSWORD LDAP USER", uid, "Set User samba password in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Setting password for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "SET SAMBA PASSWORD LDAP USER", uid, "Set User samba password failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}		
	}

	public void deletePassword(String uid) {
		for (Ldap ldap : connectionManager.getConnections()) {
			try {
				String ldapDn = "uid=" + uid + "," + ldapUserBase;
				Attributes attrs = ldap.getAttributes(ldapDn);
				Attribute attr = attrs.get("userPassword");
				if (attr != null) {
					ldap.modifyAttributes(ldapDn, AttributeModification.REMOVE, 
							AttributesFactory.createAttributes("userPassword"));
				}
				logger.info("Delete password for User {} in ldap {}", 
						new Object[] {uid, ldapUserBase});
				auditor.logAction("", "DELETE PASSWORD LDAP USER", uid, "Delete User password in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Setting password for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				auditor.logAction("", "DELETE PASSWORD LDAP USER", uid, "Delete User password failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}		
	}
		
	public void getInfo(Infotainment info, String uid) {
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);
		info.setRoot(root);
		
		int i = 0;
		int fail = 0;
		for (Ldap ldap : connectionManager.getConnections()) {
			i++;
			try {
				String ldapDn = "uid=" + uid + "," + ldapUserBase;
				Attributes attrs = ldap.getAttributes(ldapDn);
				new InfotainmentTreeNode("Server " + i, "Fetching Account success", root);
				
			} catch (NamingException e) {
				logger.warn("FAILED: Getting info for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				new InfotainmentTreeNode("Server " + i, "Fetching Account failed", root);
				fail++;
			}
		}
		
		if (fail == 0)
			info.setMessage("Fetching Account from " + i + " Server(s): Success");
		else if (fail < i)
			info.setMessage("Fetching Account from " + i + " Server(s): Partially failed!");
		else
			info.setMessage("Fetching Account from " + i + " Server(s): Failed!");
	}
		
	public void getInfoForAdmin(Infotainment info, String uid) {
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);
		info.setRoot(root);
		
		int i = 0;
		int fail = 0;
		for (Ldap ldap : connectionManager.getConnections()) {
			i++;
			try {
				String ldapDn = "uid=" + uid + "," + ldapUserBase;
				Attributes attrs = ldap.getAttributes(ldapDn);
				InfotainmentTreeNode ldapNode = new InfotainmentTreeNode(ldap.getLdapConfig().getLdapUrl(), "Fetching attributes ok", root);
				NamingEnumeration<?> attrEnumeration = attrs.getAll();
				while (attrEnumeration.hasMoreElements()) {
					Attribute attr = (Attribute) attrEnumeration.nextElement();
					new InfotainmentTreeNode(attr.getID(), attr.get().toString(), ldapNode);
				}					
				
				Iterator<SearchResult> iterator = ldap.search(ldapGroupBase, new SearchFilter("(memberUid=" + uid + ")"), 
						new String[] {"gidNumber", "cn"});

				InfotainmentTreeNode ldapGroupNode = new InfotainmentTreeNode(ldap.getLdapConfig().getLdapUrl(), "Fetching groups ok", root);

				while (iterator.hasNext()) {
					SearchResult sr = iterator.next();
					Attributes groupAttrs = sr.getAttributes();
					if (groupAttrs.get("gidNumber") != null && groupAttrs.get("gidNumber").get() != null &&
							groupAttrs.get("cn") != null && groupAttrs.get("cn").get() != null) {
						new InfotainmentTreeNode(groupAttrs.get("gidNumber").get().toString(), groupAttrs.get("cn").get().toString(), ldapGroupNode);
					}
				}
			} catch (NamingException e) {
				logger.warn("FAILED: Getting info for User {} in ldap {}: {}", 
						new Object[] {uid, ldapUserBase, e.getMessage()});
				new InfotainmentTreeNode(ldap.getLdapConfig().getLdapUrl(), "Fetching attributes failed", root);
				fail++;
			}
		}
		
		if (fail == 0)
			info.setMessage("Fetching Account from " + i + " Server(s): Success");
		else if (fail < i)
			info.setMessage("Fetching Account from " + i + " Server(s): Partially failed!");
		else
			info.setMessage("Fetching Account from " + i + " Server(s): Failed!");
	}
		
	public void closeConnections() {
		connectionManager.closeConnections();
	}
	
	private void addAttrIfNotExists(Attributes attrs, String key, String value, List<ModificationItem> modList) 
			throws NamingException {
		Attribute attr = attrs.get(key);

		if (attr == null) {
			modList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, 
					AttributesFactory.createAttribute(key, value)));
		}
		else {
			for (int i=0; i<attr.size(); i++) {
				if (value.equals(attr.get(i))) {
					return;
				}
			}
			modList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, 
					AttributesFactory.createAttribute(key, value)));
		}
	}
	
	private void compareAttr(Attributes attrs, String key, String value, List<ModificationItem> modList,
				StringBuilder log)
			throws NamingException {
		Attribute attr = attrs.get(key);
		
		if (attr == null) {
			modList.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, 
					AttributesFactory.createAttribute(key, value)));
			log.append("ADD: ");
			log.append(key);
			log.append("=");
			log.append(value);
			log.append(" ");
		}
		else if (! attr.get().equals(value)) {
			modList.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, 
					AttributesFactory.createAttribute(key, value)));
			log.append("REPLACE: ");
			log.append(key);
			log.append("=");
			log.append(attr.get());
			log.append("->");
			log.append(value);
			log.append(" ");
		}
	}
	
	private void createUserIntern(Ldap ldap, String cn, String givenName, String sn, String mail, String uid, String uidNumber, String gidNumber,
			String homeDir, String description, Map<String, String> extraAttributesMap) throws NamingException {
		Attributes attrs;
                
		if (ldapUserObjectclasses == null || ldapUserObjectclasses.trim().isEmpty())
			ldapUserObjectclasses = "top person organizationalPerson inetOrgPerson posixAccount";
		
		if (sambaEnabled) {
			if (!ldapUserObjectclasses.matches("(?:\\s|.)*?\\bsambaSamAccount\\b(?:\\s|.)*"))
				ldapUserObjectclasses += " sambaSamAccount";
                        
			attrs = AttributesFactory.createAttributes("objectClass",
                                ldapUserObjectclasses.split("\\s+"));
			attrs.put(AttributesFactory.createAttribute("sambaSID", sidPrefix + (Long.parseLong(uidNumber) * 2L + 1000L)));
		}
		else {
			attrs = AttributesFactory.createAttributes("objectClass",
                                ldapUserObjectclasses.split("\\s+"));
		}
		
		attrs.put(AttributesFactory.createAttribute("cn", cn));
		attrs.put(AttributesFactory.createAttribute("sn", sn));
		attrs.put(AttributesFactory.createAttribute("givenName", givenName));
		attrs.put(AttributesFactory.createAttribute("mail", mail));
		attrs.put(AttributesFactory.createAttribute("uid", uid));
		attrs.put(AttributesFactory.createAttribute("uidNumber", uidNumber));
		attrs.put(AttributesFactory.createAttribute("gidNumber", gidNumber));
		attrs.put(AttributesFactory.createAttribute("homeDirectory", homeDir));
		attrs.put(AttributesFactory.createAttribute("description", description));

		if (extraAttributesMap != null) {
			for (Entry<String, String> extraAttribute : extraAttributesMap.entrySet()) {
				if (extraAttribute.getKey().startsWith("extra_") && extraAttribute.getKey().length() > 6) {
					attrs.put(AttributesFactory.createAttribute(extraAttribute.getKey().substring(6), extraAttribute.getValue()));
				}
			}
		}
		
		ldap.create("uid=" + uid + "," + ldapUserBase, attrs);
	}
	
	private void createGroupIntern(Ldap ldap, String cn, String gidNumber) 
			throws NamingException {

		Attributes attrs;
                
		if (ldapGroupObjectclasses == null || ldapGroupObjectclasses.trim().isEmpty())
			ldapGroupObjectclasses = "top posixGroup";
		
		if (sambaEnabled && (! ldapGroupObjectclasses.matches("(?:\\s|.)*?\\bsambaGroupMapping\\b(?:\\s|.)*"))) {
			ldapGroupObjectclasses += " sambaGroupMapping";
		}

		/*
		 * In order to this for work, the ldap schema must be modified. The standard OpenLDAP core schema will fail,
		 * because of groupOfNames MUST member. This prevents the creation of empty groups. member must be in the MAY section.
		 * 
		 * Second is posixGroup being STRUCTURAL. This conflicts with groupOfNames also being STRUCTURAL. 
		 * It should be AUXILIARY like posixAccount. If you want this to work, you must change posixGroup to AUXILIARY
		 */
		if (ldapGroupType != null && ldapGroupType.equals("member")) {
			ldapGroupObjectclasses += " groupOfNames";
		}

		attrs = AttributesFactory.createAttributes("objectClass",
                ldapGroupObjectclasses.split("\\s+"));

		if (sambaEnabled) {
			attrs.put(AttributesFactory.createAttribute("sambaSID", sidPrefix + (Long.parseLong(gidNumber) * 2L + 1000L)));					
			attrs.put(AttributesFactory.createAttribute("sambaGroupType", "2"));
		}

		attrs.put(AttributesFactory.createAttribute("cn", cn));
		attrs.put(AttributesFactory.createAttribute("gidNumber", gidNumber));

		ldap.create("cn=" + cn + "," + ldapGroupBase, attrs);
	}
}
