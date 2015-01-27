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

import java.util.Map;

public abstract class AbstractExecutableJob implements ExecutableJob {

	private static final long serialVersionUID = 1L;

	private Map<String, String> jobStore;
	
	@Override
	public void setJobStore(Map<String, String> jobStore) {
		this.jobStore = jobStore;
	}

	protected Map<String, String> getJobStore() {
		return jobStore;
	}


	
}
