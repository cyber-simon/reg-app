/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 *     Sven Siebler  - Samba modifications for Service SDS@hd
 ******************************************************************************/
package edu.kit.scc.webreg.service.reg.samba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import edu.kit.scc.webreg.service.reg.ldap.LdapConnectionManager;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;
import edu.vt.middleware.ldap.AttributesFactory;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.Ldap.AttributeModification;
import edu.vt.middleware.ldap.SearchFilter;

public class Samba4Worker {

	private static Logger logger = LoggerFactory.getLogger(Samba4Worker.class);
	
	private String ldapUserBase;
	private String ldapGroupBase;
	
	private LdapConnectionManager connectionManager;
	
	private Auditor auditor;

	public Samba4Worker(PropertyReader prop, Auditor auditor) throws RegisterException {
		this.auditor = auditor;
		
		try {
			connectionManager = new LdapConnectionManager(prop);
			ldapUserBase = prop.readProp("ldap_user_base");
			ldapGroupBase = prop.readProp("ldap_group_base");

			//if (sambaEnabled)
			//	sidPrefix = prop.readProp("sid_prefix");
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		}		
	}

        /**
         * Aufrufen eines externen Tools zum ausführen von LDAP relevanten Abfragen
         * 
         * @param cmd Kommando inkl. zusätzlichen Parametern
         * @param msg Text der in Logmeldungen verwendet werden soll
         * @param uid 
         * @return 
         */
        private String externalCall(String[] cmd, String msg, String uid) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);                        
                Process p = pb.start();
                BufferedReader reader = new BufferedReader (new InputStreamReader(p.getInputStream()));                            
                p.waitFor();
                String line = reader.readLine();
                String logline="";
                while (line != null && ! line.trim().equals("--EOF--")) {
                    logger.debug("console: {}",line);
                    logline += " " + line;
                    line = reader.readLine();
                }
                auditor.logAction("", msg.toUpperCase() + ": SUCCESS:", uid, msg + ": "+logline, AuditStatus.SUCCESS);
                return logline;
            } catch (IOException|InterruptedException e) {
                logger.warn("{} FAILED: {}", 
                                    new Object[] {msg, e.getMessage()});
//                auditor.logAction("", "SET SAMBA PASSWORD LDAP USER", uid, "Set User samba password failed: " + e.getMessage(), AuditStatus.FAIL);
                return "FAILED";
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

			// SS: es muss anschließen auf Konsole die DB repariert werden: "samba-tool dbcheck --fix" um einen fehlerhaften Memberverweis zu entfernen
            logger.debug("Samba4 Delete User: repair groupmemberships..."); 
            externalCall(new String[]{"/home/wildfly/repair_groups"}, "Samba Delete User", uid);
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
                                String memberUid_Identifier = "memberUid";
                                
                                // SS: Samba 4 AD legt die Infos bei der Gruppe im Attribut member als "UID=xxxx,dc..." ab (anstatt in memberUid als XXX)
                                memberUid_Identifier = "member";   

                                Attributes attrs = ldap.getAttributes(dn, new String[]{memberUid_Identifier});
                                Attribute attr = attrs.get(memberUid_Identifier);
				if (attr != null) {
                                    for (int i=0; i<attr.size(); i++) {
                                        String memberUid = (String) attr.get(i);
                                        memberUid = memberUid.substring(memberUid.indexOf("UID=")+4, memberUid.indexOf(","));
                                        oldMemberUids.add(memberUid);
                                    }
				}
				
				Set<String> addMemberUids = new HashSet<String>(memberUids);
				addMemberUids.removeAll(oldMemberUids);

				Set<String> removeMemberUids = new HashSet<String>(oldMemberUids);
				removeMemberUids.removeAll(memberUids);

				for (String memberUid : addMemberUids) {
                                    memberUid = "uid="+memberUid+","+ldapUserBase;

                                    logger.info("Adding member {} to group {}", memberUid, cn);
                                    try {
                                            ldap.modifyAttributes(dn, AttributeModification.ADD, 
                                                            AttributesFactory.createAttributes(memberUid_Identifier, memberUid));
                                            auditor.logAction(cn, "ADD LDAP GROUP MEMBER", memberUid, "Added member on " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
                                    } catch (NamingException e) {
                                            auditor.logAction(cn, "ADD LDAP GROUP MEMBER", memberUid, "Add member on " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
                                    }
				}
				
				for (String memberUid : removeMemberUids) {
                                    memberUid = "uid="+memberUid+","+ldapUserBase;

                                    logger.info("Removing member {} from group {}", memberUid, cn);
                                    try {
                                            ldap.modifyAttributes(dn, AttributeModification.REMOVE, 
                                                            AttributesFactory.createAttributes(memberUid_Identifier, memberUid));
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
						new Object[] {cn, ldapGroupBase});
				auditor.logAction("", "DELETE LDAP GROUP", cn, "Group deletion failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
		}
	}

	public void reconUser(String cn, String sn, String givenName, String mail, String uid, String uidNumber, String gidNumber,
			String homeDir, String description) {
		for (Ldap ldap : connectionManager.getConnections()) {
			List<String> dnList = new ArrayList<String>();
                        
			try {
                            // zus. Begrenzung des Suchraums auf Userbase verhindert beim Samba 4 einen Fehler mit "Unprocessed Continuation Reference(s)"
                            Iterator<SearchResult> iterator = ldap.search(ldapUserBase,new SearchFilter("uidNumber="+uidNumber), new String[] {"uid"});
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
					createUserIntern(ldap, cn, givenName, sn, mail, uid, uidNumber, gidNumber, homeDir, description);
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

					compareAttr(attrs, "homeDirectory", homeDir, modList, log);
					compareAttr(attrs, "description", description, modList, log);
					
                    // SS: im RFC2307 Schema gar nicht vorgesehen ? Wird wahrscheinlich nur für Samba Komaptibilät in einem beliebigen LDAP gebraucht
                    // -> Samba 4 braucht das nicht
					//addAttrIfNotExists(attrs, "objectClass", "sambaSamAccount", modList);
					//compareAttr(attrs, "sambaSID", sidPrefix + (Long.parseLong(uidNumber) * 2L + 1000L), modList, log);	
                                                
                    // SS: nss_winbind + idmap_ad liest zwangsweise genau dieses Attribut als Gruppe aus
                    // @todo: aus der GIDnumber die RID holen
//                                                String rid = "1118";
//                                                compareAttr(attrs, "primaryGroupID", rid, modList, log);	
                                                // SS: RFC2307-Schema bildet zwingend das AD-Attribut samAccountName auf den Unix-Usernamen ab
                    compareAttr(attrs, "samAccountName", uid, modList, log);	
					
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
                                                // SS: uid gar nicht unterstützt in Group bzw. posixGroup Schema
						//if (sambaEnabled) {
						//	ldap.modifyAttributes(newDn, AttributeModification.REPLACE, 
						//		AttributesFactory.createAttributes("uid", cn));
						//}
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
						new Object[] {cn, gidNumber, ldapGroupBase});
				auditor.logAction("", "CREATE LDAP GROUP", cn, "Group created in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.SUCCESS);
			} catch (NamingException e) {
				logger.warn("FAILED: Group cn:{}, gid:{} with ldap {}: {}", 
						new Object[] {cn, gidNumber, 
						ldapGroupBase, e.getMessage()});
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
	/**
         * Problem: die Nutzerpasswörter in Samba 4 sind nicht direkt per LDAP modifizierbar 
         * (http://unix.stackexchange.com/questions/73365/where-does-samba-4-store-user-passwords)
         * 
         * @param uid
         * @param passwordhash 
         * @param user 
         */
	public void setSambaPassword(String uid, String passwordhash, UserEntity user) {
            externalCall(new String[]{"/home/wildfly/change_passwd", uid, passwordhash}, "Set Samba Password", uid);
	}

	public void deletePassword(String uid) {
        // SS: Passwort löschen erfolgt durch deaktivieren des Nutzers im Samba AD
        externalCall(new String[]{"/home/wildfly/change_passwd", uid}, "Delete Samba Password", uid);
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
			String homeDir, String description) throws NamingException {
		Attributes attrs;
	                
        // SS: im RFC2307 Schema gar nicht vorgesehen ? Wird wahrscheinlich nur für Samba Komaptibilät in einem beliebigen LDAP gebraucht
        // -> Samba 4 braucht das nicht 
        // -> inetOrgPerson haben im Samba 4 eigentlich keine UNIX attribute -> deswegen besser nur anlegen als person
        // https://social.technet.microsoft.com/Forums/windowsserver/en-US/2691a35f-110a-445e-bb63-37d801b796f6/why-does-the-unix-attributes-tab-disappear-for-inetorgperson-classed-objects?forum=winserverDS
        // ohne inetOrgPerson schlägt allerdings das anlegen eines Accounts per regApp fehl - noch k.A. warum
		attrs = AttributesFactory.createAttributes("objectClass", new String[] {
			"top", "person", "organizationalPerson", 
			"inetOrgPerson","posixAccount"}); // "sambaSamAccount"

        // SS: in Samba 4 ersetzt durch objectSid
		// attrs.put(AttributesFactory.createAttribute("sambaSID", sidPrefix + (Long.parseLong(uidNumber) * 2L + 1000L)));
                                                        
        // SS: nss_winbind + idmap_ad liest zwangsweise genau dieses Attribut als Gruppe aus
        // PrimaryGroup kann aber nicht beim Anlegen geändert/gesetzt werden! Muss nachträglich direkt an der Gruppe passieren
        //attrs.put(AttributesFactory.createAttribute("primaryGroupID", gidNumber));
        
        // SS: RFC2307-Schema bildet zwingend das AD-Attribut samAccountName auf den Unix-Usernamen ab
        attrs.put(AttributesFactory.createAttribute("samAccountName", uid));
		
		attrs.put(AttributesFactory.createAttribute("cn", cn));
		attrs.put(AttributesFactory.createAttribute("sn", sn));
		attrs.put(AttributesFactory.createAttribute("givenName", givenName));
		attrs.put(AttributesFactory.createAttribute("mail", mail));
		attrs.put(AttributesFactory.createAttribute("uid", uid));
		attrs.put(AttributesFactory.createAttribute("uidNumber", uidNumber));
                //! SS: gidNumber ist beim Nutzer im Samba 4 AD nicht vorgesehen -> memberOf
                //! wird aber für kompatibilität zur LDAP Abfragen trotzdem benötigt
                attrs.put(AttributesFactory.createAttribute("gidNumber", gidNumber));
		attrs.put(AttributesFactory.createAttribute("homeDirectory", homeDir));
		attrs.put(AttributesFactory.createAttribute("description", description));

		ldap.create("uid=" + uid + "," + ldapUserBase, attrs);
                
		// SS: nach dem Anlegen eines Nutzers muss seine Primärgruppe angepasst werden
		logger.debug("Samba4 Create User: group modifications...");

		// Set up environment for creating initial context
		Iterator<SearchResult> resultIterator = ldap.search(ldapGroupBase,
				new SearchFilter("(gidNumber=" + gidNumber + ")"), new String[] { "cn", "objectSid" });
		List<SearchResult> resultList = IteratorUtils.toList(resultIterator);
		if (resultList.size() > 1) {
			logger.warn("More than one Ldap Group ({}) with gid {} found!", resultList.size(), gidNumber);

		} else if (resultList.size() > 0) {
			SearchResult sr = resultList.get(0);
			Attribute cnAttr = sr.getAttributes().get("cn");
			String pgname = (String) cnAttr.get(); // ! hd_hd, etc
			String objectSid = externalCall(new String[] { "/opt/samba/bin/wbinfo", "--gid-to-sid", gidNumber },
					"Samba Create User", uid);
			String actualRID = objectSid.substring(objectSid.lastIndexOf('-') + 1);
			logger.debug("Samba4 Create User: found for gidnumber {} the group {} with RID {} ",
					new Object[] { gidNumber, pgname, actualRID });

			try {
				// Primärgruppe von Domain Users zu HomeOrgGroup ändern
				// Nutzer muss vorher allerdings schon member der zukünftigen primaryGroup sein
				// !!
				ldap.modifyAttributes("cn=" + pgname + "," + ldapGroupBase, AttributeModification.ADD,
						AttributesFactory.createAttributes("member", "uid=" + uid + "," + ldapUserBase));
				ldap.modifyAttributes("uid=" + uid + "," + ldapUserBase, AttributeModification.REPLACE,
						AttributesFactory.createAttributes("primaryGroupID", actualRID));
			} catch (NamingException e) {
				logger.warn("FAILED: changing group for User {} in ldap {}: {}",
						new Object[] { uid, ldapUserBase, e.getMessage() });
				auditor.logAction("", "CHANGE PRIMARYGROUPID FAILED", uid,
						"changing primarygroupid failed in " + ldap.getLdapConfig().getLdapUrl(), AuditStatus.FAIL);
			}
			// ! Nutzer wieder aus Kompatibilitätsgründen in Domain Users aufnehmen
			logger.debug("Samba4 Create User: add Member to Domain Users group...");
			try {
				ldap.modifyAttributes("cn=Domain Users," + ldapUserBase, AttributeModification.ADD,
						AttributesFactory.createAttributes("member", "uid=" + uid + "," + ldapUserBase));
			} catch (NamingException e) {
				logger.warn("FAILED: adding member to domain user {} in ldap {}: {}",
						new Object[] { uid, ldapUserBase, e.getMessage() });
				auditor.logAction("", "adding to domain user failed", uid,
						"adding user to domain members failed in " + ldap.getLdapConfig().getLdapUrl(),
						AuditStatus.FAIL);
			}
			// SS: es muss anschließend auf Konsole die DB repariert werden: "samba-tool
			// dbcheck --fix" um einen fehlerhaften Memberverweis zu entfernen
			logger.debug("Samba4 Create User: repair groupmemberships...");
			externalCall(new String[] { "/home/wildfly/repair_groups" }, "Samba Create User (repair groups)", uid);
		} else {
			// ! SS: Gruppe gibts gar nicht ??
			logger.debug("Samba4 Create User: no valid group found for gidnumber {}", new Object[] { gidNumber });
			auditor.logAction("", "SAMBA CREATE USER FAILED", uid,
					"Samba4 Create User failed: no group found for gidnumber " + gidNumber, AuditStatus.FAIL);
		}
	}
	
	private void createGroupIntern(Ldap ldap, String cn, String gidNumber) 
			throws NamingException {

		Attributes attrs;
		
		attrs = AttributesFactory.createAttributes("objectClass", new String[] {
				"top", "group","posixGroup"}); // "sambaSamAccount", "sambaGroupMapping"
        // SS: in Samba 4 nicht benötigt
		//attrs.put(AttributesFactory.createAttribute("sambaSID", sidPrefix + (Long.parseLong(gidNumber) * 2L + 1000L)));
                    
        // @todo: uid wird weder in group, noch posixGroup unterstützt. Wirklich gebraucht ??
		//attrs.put(AttributesFactory.createAttribute("uid", cn));

		// SS: RFC2307-Schema bildet zwingend das AD-Attribut samAccountName auf den Unix-Usernamen ab
        attrs.put(AttributesFactory.createAttribute("samAccountName", cn));
                    
        // SS: ist das getestet mit Samba4 ?
        attrs.put(AttributesFactory.createAttribute("sambaGroupType", "2"));    

		attrs.put(AttributesFactory.createAttribute("cn", cn));
		attrs.put(AttributesFactory.createAttribute("gidNumber", gidNumber));

		ldap.create("cn=" + cn + "," + ldapGroupBase, attrs);
	}
}
