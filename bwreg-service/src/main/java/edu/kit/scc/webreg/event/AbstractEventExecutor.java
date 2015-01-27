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

import edu.kit.scc.webreg.job.AbstractExecutableJob;

public abstract class AbstractEventExecutor<T extends Event<E>, E extends Serializable> 
		extends AbstractExecutableJob
		implements EventExecutor<T, E> {

	private static final long serialVersionUID = 1L;

	private T event;

	public AbstractEventExecutor() {
		super();
	}

	public T getEvent() {
		return event;
	}

	public void setEvent(T event) {
		this.event = event;
	}		
}
