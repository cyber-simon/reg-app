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

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import edu.kit.scc.webreg.job.ExecutableJob;

@Stateless
public class AsyncJobExecutorImpl implements AsyncJobExecutor {

	@Override
	@Asynchronous
	public void execute(ExecutableJob job) {
		job.execute();
	}
	
}
