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
package edu.kit.scc.webreg.service.timer;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.TimerService;

public interface ClusterScheduler extends Serializable{

	void stopTimers();

	void startTimers(String nodeName);

	TimerService getTimerService();

	String getNodeName();

	Date getTimerConfigured();

}
