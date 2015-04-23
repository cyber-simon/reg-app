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

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jcifs.util.Hexdump;
import jcifs.util.MD4;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.GroupCapable;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import edu.kit.scc.webreg.service.reg.impl.GroupUpdateStructure;

public abstract class AbstractLdapRegisterWorkflow 
		implements RegisterUserWorkflow, SetPasswordCapable, InfotainmentCapable, GroupCapable {

	protected static Logger logger = LoggerFactory.getLogger(AbstractLdapRegisterWorkflow.class);
	
	protected abstract String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructLocalUid(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructGroupName(GroupEntity group);
	protected abstract Boolean isSambaEnabled();
	
	@Override
	public void registerUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		reconciliation(user, service, registry, auditor);
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("AbstractLdapRegister Deregister user {} for service {}", user.getEppn(), service.getName());

		PropertyReader prop = new PropertyReader(service.getServiceProps());

		Map<String, String> regMap = registry.getRegistryValues();

		String localUid = regMap.get("localUid");

		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());
		ldapWorker.deleteUser(localUid);
		ldapWorker.closeConnections();
	}

	@Override
	public void updateGroups(ServiceEntity service, GroupUpdateStructure updateStruct, Auditor auditor)
			throws RegisterException {

		PropertyReader prop = new PropertyReader(service.getServiceProps());
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
		
		PropertyReader prop = new PropertyReader(service.getServiceProps());
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());

		ldapWorker.deleteGroup(constructGroupName(group));		
		
		ldapWorker.closeConnections();
		
	}
	
	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

		PropertyReader prop = new PropertyReader(service.getServiceProps());
		
		/*
		 * Compare values from user and registry store. Found differences trigger 
		 * a full reconsiliation
		 */
		Map<String, String> reconMap = new HashMap<String, String>();
		
		String homeId = user.getAttributeStore().get("http://bwidm.de/bwidmOrgId");
		if (prop.hasProp("tpl_home_id")) {
			homeId = evalTemplate(prop.readProp("tpl_home_id"), user, reconMap, homeId, null);
		}

		String homeUid = user.getAttributeStore().get("urn:oid:0.9.2342.19200300.100.1.1");
		homeId = homeId.toLowerCase();
		if (prop.hasProp("tpl_home_uid")) {
			homeId = evalTemplate(prop.readProp("tpl_home_uid"), user, reconMap, homeId, homeUid);
		}
		
		if (prop.hasProp("tpl_cn")) {
			reconMap.put("cn", evalTemplate(prop.readProp("tpl_cn"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("cn", user.getEppn());
		}
		
		if (prop.hasProp("tpl_sn")) {
			reconMap.put("sn", evalTemplate(prop.readProp("tpl_sn"), user, reconMap, homeId, homeUid));
		}
		else {
			if (user.getSurName() != null)
				reconMap.put("sn", user.getSurName());
			else
				reconMap.put("sn", "Unknown");
		}
		
		if (prop.hasProp("tpl_given_name")) {
			reconMap.put("givenName", evalTemplate(prop.readProp("tpl_given_name"), user, reconMap, homeId, homeUid));
		}
		else {
			if (user.getGivenName() != null)
				reconMap.put("givenName", user.getGivenName());
			else 
				reconMap.put("givenName", "Unknown");
		}
		
		if (prop.hasProp("tpl_mail")) {
			reconMap.put("mail", evalTemplate(prop.readProp("tpl_mail"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("mail", user.getEmail());
		}
		
		reconMap.put("uidNumber", "" + user.getUidNumber());
		reconMap.put("gidNumber", "" + user.getPrimaryGroup().getGidNumber());
		reconMap.put("description", registry.getId().toString());
		
		reconMap.put("groupName", constructGroupName(user.getPrimaryGroup()));
		
		if (prop.hasProp("tpl_local_uid")) {
			reconMap.put("localUid", evalTemplate(prop.readProp("tpl_local_uid"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("localUid", constructLocalUid(homeId, homeUid, user, reconMap));
		}
		
		if (prop.hasProp("tpl_home_dir")) {
			reconMap.put("homeDir", evalTemplate(prop.readProp("tpl_home_dir"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("homeDir", constructHomeDir(homeId, homeUid, user, reconMap));
		}

		reconMap.put("sambaEnabled", isSambaEnabled().toString());
		
		Boolean change = false;
		
		for (Entry<String, String> entry : reconMap.entrySet()) {
			if (! registry.getRegistryValues().containsKey(entry.getKey())) {
				auditor.logAction("", "UPDATE USER REGISTRY", user.getEppn(), "ADD " + 
						entry.getKey() + ": " + registry.getRegistryValues().get(entry.getKey()) +
						" => " + entry.getValue()
						, AuditStatus.SUCCESS);
				registry.getRegistryValues().put(entry.getKey(), entry.getValue());
				change |= true;
			}
			else if (! registry.getRegistryValues().get(entry.getKey()).equals(entry.getValue())) {
				auditor.logAction("", "UPDATE USER REGISTRY", user.getEppn(), "REPLACE " + 
						entry.getKey() + ": " + registry.getRegistryValues().get(entry.getKey()) +
						" => " + entry.getValue()
						, AuditStatus.SUCCESS);
				registry.getRegistryValues().put(entry.getKey(), entry.getValue());
				change |= true;
			}
		}
		
		return change;
	}	
	
	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		logger.info("LDAP Reconsiliation user {} for service {}", user.getEppn(), service.getName());

		PropertyReader prop = new PropertyReader(service.getServiceProps());
		Map<String, String> regMap = registry.getRegistryValues();
		
		String cn = regMap.get("cn");
		String sn = regMap.get("sn");
		String givenName = regMap.get("givenName");
		String mail = regMap.get("mail");
		String localUid = regMap.get("localUid");
		String uidNumber = regMap.get("uidNumber");
		String gidNumber = regMap.get("gidNumber");
		String homeDir = regMap.get("homeDir");
		String description = registry.getId().toString();
		
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());

		ldapWorker.reconUser(cn, sn, givenName, mail, localUid, uidNumber, gidNumber, homeDir, description);
		
		ldapWorker.closeConnections();
	}

	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password) throws RegisterException {
		logger.debug("Setting service password for user {}", user.getEppn());

		PropertyReader prop = new PropertyReader(service.getServiceProps());
		Map<String, String> regMap = registry.getRegistryValues();

		String passwordRegex;
		if (prop.hasProp("password_regex")) 
			passwordRegex = prop.readProp("password_regex");
		else
			passwordRegex = ".{6,}";

		String passwordRegexMessage;
		if (prop.hasProp("password_regex_message")) 
			passwordRegexMessage = prop.readProp("password_regex_message");
		else
			passwordRegexMessage = "Das Passwort ist nicht komplex genug";
		
		if (! password.matches(passwordRegex))
			throw new RegisterException(passwordRegexMessage);

		String localUid = regMap.get("localUid");

		String ntPassword = null;
		
		if (isSambaEnabled())
			ntPassword = calcNtPassword(password);

		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());
		ldapWorker.setPassword(localUid, password);

		if (isSambaEnabled())
			ldapWorker.setSambaPassword(localUid, ntPassword, user);
		
		ldapWorker.closeConnections();		
	}

	@Override
	public void deletePassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		PropertyReader prop = new PropertyReader(service.getServiceProps());
		Map<String, String> regMap = registry.getRegistryValues();
		String localUid = regMap.get("localUid");
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());
		ldapWorker.deletePassword(localUid);
		ldapWorker.closeConnections();		
	}	
	
	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		Infotainment info = new Infotainment();
		
		PropertyReader prop = new PropertyReader(service.getServiceProps());
		Map<String, String> regMap = registry.getRegistryValues();
		String localUid = regMap.get("localUid");
		LdapWorker ldapWorker = new LdapWorker(prop, null, isSambaEnabled());

		ldapWorker.getInfo(info, localUid);
		
		ldapWorker.closeConnections();		

		return info;
	}		

	private String calcNtPassword(String password) {
		String ntHash = "";
		MD4 md4 = new MD4();
		byte[] bpass;
		try {
			bpass = password.getBytes("UnicodeLittleUnmarked");

			md4.engineUpdate(bpass, 0, bpass.length);
			byte[] hashbytes = new byte[32];
			hashbytes = md4.engineDigest();
			ntHash = new String(Hexdump.toHexString(hashbytes, 0,
					hashbytes.length * 2));

			return ntHash;
		} catch (UnsupportedEncodingException e) {
			logger.warn("Calculating NT Password failed!", e);
			return "";
		}	
	}
	
	private String evalTemplate(String template, UserEntity user, Map<String, String> reconMap, String homeId, String homeUid) 
			throws RegisterException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.init();
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", user);
		context.put("reconMap", reconMap);
		context.put("homeId", homeId);
		context.put("homeUid", homeUid);
		VelocityContext velocityContext = new VelocityContext(context);
		StringWriter out = new StringWriter();

		try {
			engine.evaluate(velocityContext, out, "log", template);
			
			return out.toString();
		} catch (ParseErrorException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		} catch (MethodInvocationException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		} catch (ResourceNotFoundException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		}
	}
}
