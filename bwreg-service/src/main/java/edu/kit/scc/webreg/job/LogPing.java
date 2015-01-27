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
package edu.kit.scc.webreg.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogPing extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(LogPing.class);
		logger.info("LogPing ping");
	}
}
