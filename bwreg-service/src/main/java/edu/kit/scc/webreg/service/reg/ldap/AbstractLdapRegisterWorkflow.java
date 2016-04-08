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

import java.io.UnsupportedEncodingException;
import java.util.Map;

import jcifs.util.Hexdump;
import jcifs.util.MD4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;

public abstract class AbstractLdapRegisterWorkflow 
		extends AbstractSimpleGroupLdapRegisterWorkflow
		implements SetPasswordCapable {

	protected static Logger logger = LoggerFactory.getLogger(AbstractSimpleLdapRegisterWorkflow.class);
	
	protected abstract String constructHomeDir(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructLocalUid(String homeId, String homeUid, UserEntity user, Map<String, String> reconMap);
	protected abstract String constructGroupName(GroupEntity group);
	protected abstract Boolean isSambaEnabled();
	
	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password) throws RegisterException {
		logger.debug("Setting service password for user {}", user.getEppn());

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();

		String passwordRegex;
		if (prop.hasProp("password_regex")) 
			passwordRegex = prop.readPropOrNull("password_regex");
		else
			passwordRegex = ".{6,}";

		String passwordRegexMessage;
		if (prop.hasProp("password_regex_message")) 
			passwordRegexMessage = prop.readPropOrNull("password_regex_message");
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
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();
		String localUid = regMap.get("localUid");
		LdapWorker ldapWorker = new LdapWorker(prop, auditor, isSambaEnabled());
		ldapWorker.deletePassword(localUid);
		ldapWorker.closeConnections();		
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
}
