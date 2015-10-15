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

import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;
import edu.vt.middleware.ldap.ssl.TLSSocketFactory;

public class LdapConnectionManager {

	private static Logger logger = LoggerFactory.getLogger(LdapConnectionManager.class);
	
	private String ldapBase;
	
	private Map<String, Ldap> connectionMap;
	
	public LdapConnectionManager(PropertyReader prop) throws PropertyReaderException {
		
		connectionMap = new HashMap<String, Ldap>();
		
		String ldapConnect = prop.readProp("ldap_connect");
		String bindDn = prop.readProp("bind_dn");
		String bindPassword = prop.readProp("bind_password");
		String connectionSecurity = prop.readProp("connection_security");
		
		int timeLimit;
		if (prop.readPropOrNull("timelimit") != null)
			timeLimit = Integer.parseInt(prop.readPropOrNull("timelimit"));
		else
			timeLimit = 30000;
		
		int timeout;
		if (prop.readPropOrNull("timeout") != null)
			timeout = Integer.parseInt(prop.readPropOrNull("timeout"));
		else
			timeout = 30000;
		
		ldapBase = prop.readProp("ldap_base");

		String[] ldapConnects = ldapConnect.split(",");

		for (String connect : ldapConnects) {
			if (connect != null && (!connect.isEmpty())) {
				connect = connect.trim();
				logger.info("Creating ldap connection for {}", connect);
				Ldap ldap = getLdapConnect(connect.trim(), ldapBase, bindDn, bindPassword, connectionSecurity, timeLimit, timeout);
				connectionMap.put(connect, ldap);
			}
		}
	}

	public Collection<Ldap> getConnections() {
		return connectionMap.values();
	}
	
	public void closeConnections() {
		for (Ldap ldap : connectionMap.values()) {
			try {
				ldap.close();
			} 
			catch (Exception e) {
				// Cannot close, ignore
			}
		}
	}
	
	private Ldap getLdapConnect(String ldapConnect, String ldapBase, 
			String bindDn, String bindPassword, String connectionSecurity, int timeLimit, int timeout) {

		logger.debug("Creating ldap connection connect: {} base: {} bind-dn: {}", new Object[] {ldapConnect, ldapBase, bindDn});
		
		LdapConfig config = new LdapConfig(ldapConnect, ldapBase);
		config.setBindDn(bindDn);
		config.setBindCredential(bindPassword);
		
		config.setTimeLimit(timeLimit);
		config.setTimeout(timeout);
		
		logger.debug("connection_security set to '{}'", connectionSecurity.toLowerCase());
		if (connectionSecurity.toLowerCase().equals("tls")) {
			logger.debug("TLS is used.");
			config.setTls(true);
		} else if(connectionSecurity.toLowerCase().equals("ssl")) {
			logger.debug("SSL is used.");
			config.setSsl(true);
		} else if(connectionSecurity.toLowerCase().equals("tls_nocheck")) {
			logger.debug("TLS is used. Certificate Hostname Check is disabled!");
			config.setTls(true);
			TLSSocketFactory tlssf = new TLSSocketFactory();
			tlssf.setHostnameVerifier(new AnyHostnameVerifier());
			try {
				tlssf.initialize();
			} catch (GeneralSecurityException e) {
				logger.warn("ssl_nocheck Problems!", e);
			}
			config.setSslSocketFactory(tlssf);
			config.setHostnameVerifier(new AnyHostnameVerifier());
		} else {
			logger.warn("Ldap Connection is not encrypted");
		}
		
		Ldap ldap = new Ldap(config);
	
		return ldap;
	}
}
