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
package edu.kit.scc.webreg.bootstrap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

@ApplicationScoped
public class NodeConfiguration {

	@Inject
	private Logger logger;
	
	private Date timerConfigured;
	
	private Date rulesConfigured;

	public Date getTimerConfigured() {
		return timerConfigured;
	}

	public void setTimerConfigured(Date timerConfigured) {
		this.timerConfigured = timerConfigured;
	}

	public Date getRulesConfigured() {
		return rulesConfigured;
	}

	public void setRulesConfigured(Date rulesConfigured) {
		this.rulesConfigured = rulesConfigured;
	}

	public String getNodeName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.warn("Exception while resolving hostname!", e);
			return System.getProperty("jboss.node.name");
		}
	}
}
