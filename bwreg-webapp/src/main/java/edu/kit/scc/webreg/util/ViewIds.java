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
package edu.kit.scc.webreg.util;

public class ViewIds {

	/*
	 * Users
	 */
	public static final String SHOW_USER = "/admin/user/show-user.xhtml";	
	public static final String LIST_USERS = "/admin/user/list-users.xhtml";	

	public static final String SHOW_ADMIN_USER = "/admin/user/show-admin-user.xhtml";	
	public static final String LIST_ADMIN_USERS = "/admin/user/list-admin-users.xhtml";	

	/*
	 * Config
	 */
	public static final String CONFIG_INDEX = "/admin/config/index.xhtml";	
	public static final String CONFIG_EDIT_RULES = "/admin/config/rules.xhtml";	
	
	/*
	 * Groups
	 */
	public static final String LIST_LOCAL_GROUPS = "/admin/group/list-local-groups.xhtml";	
	public static final String LIST_HOMEORG_GROUPS = "/admin/group/list-homeorg-groups.xhtml";	

	public static final String GROUP_ADMIN_INDEX = "/service-group-admin/index.xhtml";	
	public static final String GROUP_ADMIN_ADD_LOCAL_GROUP = "/service-group-admin/add-local-group.xhtml";	
	public static final String GROUP_ADMIN_EDIT_LOCAL_GROUP = "/service-group-admin/edit-local-group.xhtml";	
	public static final String GROUP_ADMIN_SHOW_LOCAL_GROUP = "/service-group-admin/show-local-group.xhtml";	
	
	/*
	 * Services
	 */
	public static final String EDIT_AGREEMENT = "/admin/service/edit-agreement.xhtml";
	public static final String EDIT_POLICY = "/admin/service/edit-policy.xhtml";	
	public static final String EDIT_SERVICE = "/admin/service/edit-service.xhtml";	
	public static final String EDIT_SERVICEDESC = "/admin/service/edit-servicedesc.xhtml";	
	public static final String EDIT_EMAIL_TEMPLATE = "/admin/service/edit-email-template.xhtml";	

	public static final String SHOW_POLICY = "/admin/service/show-policy.xhtml";	
	public static final String SHOW_SERVICE = "/admin/service/show-service.xhtml";	
	public static final String SHOW_EMAIL_TEMPLATE = "/admin/service/show-email-template.xhtml";	

	public static final String ADD_AGREEMENT = "/admin/service/add-agreement.xhtml";
	public static final String ADD_POLICY = "/admin/service/add-policy.xhtml";	
	public static final String ADD_SERVICE = "/admin/service/add-service.xhtml";	
	public static final String ADD_EMAIL_TEMPLATE = "/admin/service/add-email-template.xhtml";	

	public static final String LIST_SERVICES = "/admin/service/list-services.xhtml";	
	public static final String LIST_EMAIL_TEMPLATES = "/admin/service/list-email-templates.xhtml";	
	
	/*
	 * Roles
	 */
	public static final String EDIT_ROLE = "/admin/role/edit-role.xhtml";	
	public static final String EDIT_ADMIN_ROLE = "/admin/role/edit-admin-role.xhtml";	
	public static final String EDIT_GROUP_ADMIN_ROLE = "/admin/role/edit-group-admin-role.xhtml";	
	public static final String EDIT_APPROVER_ROLE = "/admin/role/edit-approver-role.xhtml";	

	public static final String SHOW_ROLE = "/admin/role/show-role.xhtml";	
	public static final String SHOW_ADMIN_ROLE = "/admin/role/show-admin-role.xhtml";	
	public static final String SHOW_GROUP_ADMIN_ROLE = "/admin/role/show-group-admin-role.xhtml";	
	public static final String SHOW_APPROVER_ROLE = "/admin/role/show-approver-role.xhtml";	

	public static final String ADD_ROLE = "/admin/role/add-role.xhtml";	
	public static final String ADD_ADMIN_ROLE = "/admin/role/add-admin-role.xhtml";	
	public static final String ADD_GROUP_ADMIN_ROLE = "/admin/role/add-group-admin-role.xhtml";	
	public static final String ADD_APPROVER_ROLE = "/admin/role/add-approver-role.xhtml";	

	public static final String LIST_ROLES = "/admin/role/list-roles.xhtml";	
	
	/*
	 * SAML Stuff
	 */
	public static final String EDIT_FEDERATION = "/admin/saml/edit-federation.xhtml";	
	public static final String EDIT_SAMLSPCONFIG = "/admin/saml/edit-sp-config.xhtml";	

	public static final String SHOW_FEDERATION = "/admin/saml/show-federation.xhtml";	
	public static final String SHOW_SAMLSPCONFIG = "/admin/saml/show-sp-config.xhtml";	

	public static final String ADD_FEDERATION = "/admin/saml/add-federation.xhtml";	
	public static final String ADD_SAMLSPCONFIG = "/admin/saml/add-sp-config.xhtml";	

	public static final String LIST_FEDERATIONS = "/admin/saml/list-federations.xhtml";	
	public static final String LIST_SAMLSPCONFIGS = "/admin/saml/list-sp-configs.xhtml";	
	public static final String LIST_APPROVALS = "/service-approver/index.xhtml";	

	/*
	 * Business Rules
	 */
	public static final String ADD_BUSINESS_RULE = "/admin/business-rule/add-business-rule.xhtml";	
	public static final String EDIT_BUSINESS_RULE = "/admin/business-rule/edit-business-rule.xhtml";	
	public static final String SHOW_BUSINESS_RULE = "/admin/business-rule/show-business-rule.xhtml";	
	public static final String LIST_BUSINESS_RULES = "/admin/business-rule/list-business-rules.xhtml";	

	public static final String ADD_RULE_PACKAGE = "/admin/business-rule/add-rule-package.xhtml";	
	public static final String EDIT_RULE_PACKAGE = "/admin/business-rule/edit-rule-package.xhtml";	
	public static final String SHOW_RULE_PACKAGE = "/admin/business-rule/show-rule-package.xhtml";	
	
	/*
	 * Timer and Schedule
	 */
	public static final String ADD_SCHEDULE = "/admin/timer/add-schedule.xhtml";	
	public static final String EDIT_SCHEDULE = "/admin/timer/edit-schedule.xhtml";	
	public static final String SHOW_SCHEDULE = "/admin/timer/show-schedule.xhtml";	
	public static final String LIST_SCHEDULES = "/admin/timer/list-schedules.xhtml";	
	
	public static final String ADD_JOBCLASS = "/admin/timer/add-job-class.xhtml";	
	public static final String EDIT_JOBCLASS = "/admin/timer/edit-job-class.xhtml";	
	public static final String SHOW_JOBCLASS = "/admin/timer/show-job-class.xhtml";	
	public static final String LIST_JOBCLASS = "/admin/timer/list-job-classes.xhtml";	
	
	/*
	 * Attribute Sources
	 */
	public static final String LIST_ATTRIBUTE_SOURCES = "/admin/as/list-attribute-sources.xhtml";	
	public static final String ADD_ATTRIBUTE_SOURCE = "/admin/as/add-attribute-source.xhtml";	
	public static final String EDIT_ATTRIBUTE_SOURCE = "/admin/as/edit-attribute-source.xhtml";	
	public static final String SHOW_ATTRIBUTE_SOURCE = "/admin/as/show-attribute-source.xhtml";	
	
	/*
	 * Public 
	 */
	public static final String IMAGE_GALLERY = "/admin/image-gallery.xhtml";	
	public static final String REGISTER_SERVICE = "/user/register-service.xhtml";	
	public static final String DEREGISTER_SERVICE = "/user/deregister-service.xhtml";	
	public static final String SERVICE_SET_PASSWORD = "/service/set-password.xhtml";	
	public static final String INDEX_USER = "/index.xhtml";	
	public static final String USER_PROPERTIES = "/user/index.xhtml";	
	public static final String REGISTER_USER = "/register.xhtml";	
	public static final String DISCOVERY_LOGIN = "/welcome/index.xhtml";	
	public static final String APPROVE_USER = "/service-approver/approve-user.xhtml";	
	
}
