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
package edu.kit.scc.webreg.event;

import java.io.Serializable;

import edu.kit.scc.webreg.job.ExecutableJob;

public interface EventExecutor<T extends Event<E>, E extends Serializable> extends ExecutableJob {

	T getEvent();
	void setEvent(T event);

}
