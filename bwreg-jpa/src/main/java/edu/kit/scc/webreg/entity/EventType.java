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
package edu.kit.scc.webreg.entity;

public enum EventType {

	/*
	 * User Events
	 */
	USER_CREATE,
	USER_UPDATE,

	/*
	 * Service Events
	 */
	SERVICE_REGISTER,
	REGISTRY_UPDATE,
	SERVICE_DEREGISTER,
	USER_LOST_ACCESS,
	USER_GAINED_ACCESS,
	APPROVAL_START,
	APPROVAL_DENIED,
	
	
	/*
	 * Group Events
	 */
	GROUP_UPDATE,
}
