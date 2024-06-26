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
	REGISTRY_PASSWORD_CHANGE,
	SERVICE_DEREGISTER,
	SERVICE_DEREGISTER_DELETE_ALL,
	USER_LOST_ACCESS,
	USER_GAINED_ACCESS,
	APPROVAL_START,
	APPROVAL_DENIED,
	
	
	/*
	 * Group Events
	 */
	GROUP_UPDATE,
	
	/*
	 * SSH Key Events
	 */
	SSH_KEY_DEPLOYED,
	SSH_KEY_DELETED,
	SSH_KEY_EXPIRED,
	SSH_KEY_REGISTRY_APPROVAL,
	SSH_KEY_REGISTRY_DEPLOYED,
	SSH_KEY_REGISTRY_DENIED,
	SSH_KEY_REGISTRY_DELETED,
	SSH_KEY_EXPIRY_WARNING,
	
	/*
	 * 2FA Events
	 */
	TWOFA_CREATED,
	TWOFA_INIT,
	TWOFA_ENABLED,
	TWOFA_DISABLED,
	TWOFA_DELETED,
	TWOFA_RESET_FAILCOUNTER,
	
	/*
	 * Project Events
	 */
	PROJECT_CRREATED,
	PROJECT_SERVICE_CREATED,
	PROJECT_SERVICE_APPROVAL,
	PROJECT_SERVICE_APPROVED,
	PROJECT_SERVICE_DENIED,
	PROJECT_SERVICE_DELETED,
	
	PROJECT_INVITATION_EMAIL_CREATED,
	PROJECT_INVITATION_EMAIL_ACCEPTED,
	PROJECT_INVITATION_EMAIL_DECLINED,
	
	PROJECT_MEMBER_ADD,
	PROJECT_MEMBER_REMOVED,
	
	/*
	 * Email Address events
	 */
	EMAIL_ADDRESS_ADDED,
	EMAIL_ADDRESS_REDO_VERIFICATION,
	EMAIL_ADDRESS_VERIFIED,
	
}
