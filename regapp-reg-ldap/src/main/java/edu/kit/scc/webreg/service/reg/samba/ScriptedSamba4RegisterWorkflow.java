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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.GroupCapable;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.ScriptingWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import edu.kit.scc.webreg.service.reg.impl.GroupUpdateStructure;
import edu.kit.scc.webreg.tools.PropertyReader;
import jcifs.util.Hexdump;
import jcifs.util.MD4;

public class ScriptedSamba4RegisterWorkflow 
		implements RegisterUserWorkflow, InfotainmentCapable, GroupCapable, SetPasswordCapable, ScriptingWorkflow {

	protected static Logger logger = LoggerFactory.getLogger(ScriptedSamba4RegisterWorkflow.class);

	protected ScriptingEnv scriptingEnv;
	
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

		Samba4Worker samba4Worker = new Samba4Worker(prop, auditor);
		samba4Worker.deleteUser(localUid);
		samba4Worker.closeConnections();
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

		try {
			String scriptName = prop.readProp("script_name");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
			
			if (scriptEntity == null)
				throw new RegisterException("service not configured properly. script is missing.");
			
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new RegisterException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				engine.eval(scriptEntity.getScript());
			
				Invocable invocable = (Invocable) engine;
				
				invocable.invokeFunction("updateRegistry", scriptingEnv, reconMap, user, registry, service, auditor, logger);
			}
			else {
				throw new RegisterException("unkown script type: " + scriptEntity.getScriptType());
			}
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		} catch (ScriptException e) {
			throw new RegisterException(e);
		} catch (NoSuchMethodException e) {
			throw new RegisterException(e);
		}
		
		if ("true".equals(prop.readPropOrNull("samba_enabled")))
			reconMap.put("sambaEnabled", "true");
		else
			reconMap.put("sambaEnabled", "false");
		
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
		String uidNumber = regMap.get("uidNumber");
		String gidNumber = regMap.get("gidNumber");
		String homeDir = regMap.get("homeDir");
		String description = registry.getId().toString();
		
		Samba4Worker samba4Worker = new Samba4Worker(prop, auditor);

		samba4Worker.reconUser(cn, sn, givenName, mail, localUid, uidNumber, gidNumber, homeDir, description, regMap);
		if (prop.hasProp("pw_location") && 
				((prop.readPropOrNull("pw_location").equalsIgnoreCase("registry")) || prop.readPropOrNull("pw_location").equalsIgnoreCase("both"))
				&& (! registry.getRegistryValues().containsKey("userPassword"))) {
			List<String> pwList = samba4Worker.getPasswords(localUid);
			if (pwList.size() > 0) {
				logger.debug("userPassword is not set in registry but in LDAP ({}). Importing from LDAP", pwList.size());
				registry.getRegistryValues().put("userPassword", pwList.get(0));
			}
		}
		
		samba4Worker.closeConnections();
	}

	protected Infotainment getInfo(RegistryEntity registry, UserEntity user,
			ServiceEntity service, Boolean forAdmin) throws RegisterException {
		Infotainment info = new Infotainment();
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();
		String localUid = regMap.get("localUid");
		Samba4Worker samba4Worker = new Samba4Worker(prop, null);

		if (forAdmin) {
			samba4Worker.getInfoForAdmin(info, localUid);
		}
		else {
			samba4Worker.getInfo(info, localUid);
		}
		
		samba4Worker.closeConnections();		

		return info;
	}
	
	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		return getInfo(registry, user, service, false);
	}		

	@Override
	public Infotainment getInfoForAdmin(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		return getInfo(registry, user, service, true);
	}

	@Override
	public void updateGroups(ServiceEntity service, GroupUpdateStructure updateStruct, Auditor auditor)
			throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Samba4Worker samba4Worker = new Samba4Worker(prop, auditor);

		try {
			String scriptName = prop.readProp("script_name");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
			
			if (scriptEntity == null)
				throw new RegisterException("service not configured properly. script is missing.");
			
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new RegisterException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				engine.eval(scriptEntity.getScript());
			
				Invocable invocable = (Invocable) engine;
		
				for (GroupEntity group : updateStruct.getGroups()) {
					long a = System.currentTimeMillis();
					Set<UserEntity> users = updateStruct.getUsersForGroup(group);
					
					logger.debug("Update Ldap Group for group {} and Service {}", group.getName(), service.getName());

					Set<String> memberUids = new HashSet<String>(users.size());

					Map<String, String> reconMap = new HashMap<String, String>();

					for (UserEntity user : users) {
						Object result = invocable.invokeFunction("resolveUid", scriptingEnv, reconMap, user, null, service, auditor, logger);
						if (result != null) {
							memberUids.add(result.toString());
						}
					}
					
					a = System.currentTimeMillis();

					Object result = invocable.invokeFunction("resolveGroupname", scriptingEnv, reconMap, group, service, auditor, logger);
					if (result != null) {
						samba4Worker.reconGroup(result.toString(), "" + group.getGidNumber(), memberUids);
					} else {
						logger.debug("Groupname for group {} did not resolve", group.getName());
					}
					
					logger.debug("reconGroup {} took {} ms", group.getName(), (System.currentTimeMillis() - a)); a = System.currentTimeMillis();
				}
				
			}
			else {
				throw new RegisterException("unkown script type: " + scriptEntity.getScriptType());
			}
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		} catch (ScriptException e) {
			throw new RegisterException(e);
		} catch (NoSuchMethodException e) {
			throw new RegisterException(e);
		}
				
		samba4Worker.closeConnections();
	}
	
	@Override
	public void deleteGroup(GroupEntity group, ServiceEntity service, Auditor auditor)
			 throws RegisterException {
		logger.debug("Delete Ldap Group for group {} and Service {}", group.getName(), service.getName());
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Samba4Worker samba4Worker = new Samba4Worker(prop, auditor);

		Map<String, String> reconMap = new HashMap<String, String>();

		try {
			String scriptName = prop.readProp("script_name");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
			
			if (scriptEntity == null)
				throw new RegisterException("service not configured properly. script is missing.");
			
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new RegisterException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				engine.eval(scriptEntity.getScript());
			
				Invocable invocable = (Invocable) engine;
		
				Object result = invocable.invokeFunction("resolveGroupname", scriptingEnv, reconMap, group, service, auditor, logger);
				if (result != null) {
					samba4Worker.deleteGroup(group.getName());		
				} else {
					logger.debug("Groupname for group {} did not resolve", group.getName());
				}
				
			}
			else {
				throw new RegisterException("unkown script type: " + scriptEntity.getScriptType());
			}
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		} catch (ScriptException e) {
			throw new RegisterException(e);
		} catch (NoSuchMethodException e) {
			throw new RegisterException(e);
		}
		
		
		samba4Worker.closeConnections();
		
	}
	
	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password) throws RegisterException {
		logger.debug("Setting service password for user {}", user.getEppn());

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();

		String localUid = regMap.get("localUid");

		String ntPassword = null;
		
		if (Boolean.parseBoolean(regMap.get("sambaEnabled")))
			ntPassword = calcNtPassword(password);

		Samba4Worker samba4Worker = new Samba4Worker(prop, auditor);
		samba4Worker.setPassword(localUid, password);

		if (Boolean.parseBoolean(regMap.get("sambaEnabled")))
			samba4Worker.setSambaPassword(localUid, ntPassword, user);
		
		samba4Worker.closeConnections();		
	}

	@Override
	public void deletePassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();
		String localUid = regMap.get("localUid");
		Samba4Worker samba4Worker = new Samba4Worker(prop, auditor);
		samba4Worker.deletePassword(localUid);
		samba4Worker.closeConnections();		
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

	@Override
	public void setScriptingEnv(ScriptingEnv scriptingEnv) {
		this.scriptingEnv = scriptingEnv;
	}
}

