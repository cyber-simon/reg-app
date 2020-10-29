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
package edu.kit.scc.webreg.service.reg.samba;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.ldap.LdapWorker;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public abstract class AbstractSimpleSamba4RegisterWorkflow 
		implements RegisterUserWorkflow, InfotainmentCapable {

	protected static Logger logger = LoggerFactory.getLogger(AbstractSimpleSamba4RegisterWorkflow.class);
	
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

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		Map<String, String> regMap = registry.getRegistryValues();

		String localUid = regMap.get("localUid");

		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());
		ldapWorker.deleteUser(localUid);
		ldapWorker.closeConnections();
	}
	
	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		
		/*
		 * Compare values from user and registry store. Found differences trigger 
		 * a full reconsiliation
		 */
		Map<String, String> reconMap = new HashMap<String, String>();
		
		String homeId = user.getAttributeStore().get("http://bwidm.de/bwidmOrgId");
		if (prop.hasProp("tpl_home_id")) {
			homeId = evalTemplate(prop.readPropOrNull("tpl_home_id"), user, reconMap, homeId, null);
		}

		String homeUid = user.getAttributeStore().get("urn:oid:0.9.2342.19200300.100.1.1");
		homeId = homeId.toLowerCase();
		if (prop.hasProp("tpl_home_uid")) {
			homeUid = evalTemplate(prop.readPropOrNull("tpl_home_uid"), user, reconMap, homeId, homeUid);
		}
		
		if (prop.hasProp("tpl_cn")) {
			reconMap.put("cn", evalTemplate(prop.readPropOrNull("tpl_cn"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("cn", user.getEppn());
		}
		
		if (prop.hasProp("tpl_sn")) {
			reconMap.put("sn", evalTemplate(prop.readPropOrNull("tpl_sn"), user, reconMap, homeId, homeUid));
		}
		else {
			if (user.getSurName() != null)
				reconMap.put("sn", user.getSurName());
			else
				reconMap.put("sn", "Unknown");
		}
		
		if (prop.hasProp("tpl_given_name")) {
			reconMap.put("givenName", evalTemplate(prop.readPropOrNull("tpl_given_name"), user, reconMap, homeId, homeUid));
		}
		else {
			if (user.getGivenName() != null)
				reconMap.put("givenName", user.getGivenName());
			else 
				reconMap.put("givenName", "Unknown");
		}
		
		if (prop.hasProp("tpl_mail")) {
			reconMap.put("mail", evalTemplate(prop.readPropOrNull("tpl_mail"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("mail", user.getEmail());
		}
		
		reconMap.put("uidNumber", "" + user.getUidNumber());
		reconMap.put("gidNumber", "" + user.getPrimaryGroup().getGidNumber());
		reconMap.put("description", registry.getId().toString());
		
		reconMap.put("groupName", constructGroupName(user.getPrimaryGroup()));
		
		if (prop.hasProp("tpl_local_uid")) {
			reconMap.put("localUid", evalTemplate(prop.readPropOrNull("tpl_local_uid"), user, reconMap, homeId, homeUid));
		}
		else {
			reconMap.put("localUid", constructLocalUid(homeId, homeUid, user, reconMap));
		}
		
		if (prop.hasProp("tpl_home_dir")) {
			reconMap.put("homeDir", evalTemplate(prop.readPropOrNull("tpl_home_dir"), user, reconMap, homeId, homeUid));
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

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();
		
		String cn = regMap.get("cn");
		String sn = regMap.get("sn");
		String givenName = regMap.get("givenName");
		String mail = regMap.get("mail");
		String localUid = regMap.get("localUid");
                
        // SS: Sonderfall durch LSDF1 Migration (HD). Es kann u.U. eine alte uidnumber vorhanden sein, die nur für den Samba 4 Fall (SDS@HD) verwendet werden muss.
        //! andere Dienste sollten aber weiterhin die reguläre bwIdm uidnumber verwenden
        String uidNumber = regMap.get("uidNumber");
        if (isSambaEnabled()) {
            if (user.getGenericStore().containsKey("uid_number_lsdf1")) {
                uidNumber = user.getGenericStore().get("uid_number_lsdf1");
                if (Integer.valueOf(uidNumber) <= 0) {
                	throw new RegisterException("Illegal BioQuant ID (<= 0)!");
                }
                logger.info("LDAP Reconsiliation - old LSDF1 BioQuant ID detected. Using BQ ID {} instead of bwIdm ID {}", uidNumber, regMap.get("uidNumber"));
                
            }
        }

        String gidNumber = regMap.get("gidNumber");
		String homeDir = regMap.get("homeDir");
		String description = registry.getId().toString();
		
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());

		ldapWorker.reconUser(cn, sn, givenName, mail, localUid, uidNumber, gidNumber, homeDir, description);
		if (prop.hasProp("pw_location") && 
				((prop.readPropOrNull("pw_location").equalsIgnoreCase("registry")) || prop.readPropOrNull("pw_location").equalsIgnoreCase("both"))
				&& (! registry.getRegistryValues().containsKey("userPassword"))) {
			List<String> pwList = ldapWorker.getPasswords(localUid);
			if (pwList.size() > 0) {
				logger.debug("userPassword is not set in registry but in LDAP ({}). Importing from LDAP", pwList.size());
				registry.getRegistryValues().put("userPassword", pwList.get(0));
			}
		}
		
		ldapWorker.closeConnections();
	}

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		Infotainment info = new Infotainment();
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();
		String localUid = regMap.get("localUid");
		LdapWorker ldapWorker = new LdapWorker(prop, null, isSambaEnabled());

		ldapWorker.getInfo(info, localUid);
		
		ldapWorker.closeConnections();		

		return info;
	}		

	protected String evalTemplate(String template, UserEntity user, Map<String, String> reconMap, String homeId, String homeUid) 
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

