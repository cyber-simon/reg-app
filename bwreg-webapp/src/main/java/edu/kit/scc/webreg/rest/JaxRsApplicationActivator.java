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
package edu.kit.scc.webreg.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import edu.kit.scc.webreg.rest.exc.AssertionExceptionMapper;
import edu.kit.scc.webreg.rest.exc.GenericRestInterfaceExceptionMapper;
import edu.kit.scc.webreg.rest.exc.LoginFailedExceptionMapper;
import edu.kit.scc.webreg.rest.exc.MisconfiguredServiceExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoDelegationConfiguredExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoEcpSupportExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoHostnameConfiguredExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoIdpForScopeExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoIdpFoundExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoItemFoundExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoRegistryFoundExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoScopedUsernameExceptionMapper;
import edu.kit.scc.webreg.rest.exc.NoUserFoundExceptionMapper;
import edu.kit.scc.webreg.rest.exc.PersistentIdExceptionMapper;
import edu.kit.scc.webreg.rest.exc.RegisterExceptionMapper;
import edu.kit.scc.webreg.rest.exc.UnauthorizedExceptionMapper;
import edu.kit.scc.webreg.rest.exc.UserCreateExceptionMapper;
import edu.kit.scc.webreg.rest.exc.UserNotRegisteredExceptionMapper;
import edu.kit.scc.webreg.rest.exc.UserUpdateFailedExceptionMapper;
import edu.kit.scc.webreg.rest.exc.ValidationExceptionMapper;

@ApplicationPath("/rest")
public class JaxRsApplicationActivator extends Application {

	   @Override
	    public Set<Class<?>> getClasses() {
	        Set<Class<?>> resources = new HashSet<>();
	        resources.add(AttributeQueryController.class);
	        resources.add(DirectAuthController.class);
	        resources.add(EcpController.class);
	        resources.add(ExternalRegistryController.class);
	        resources.add(ExternalUserController.class);
	        resources.add(GroupController.class);
	        resources.add(ImageController.class);
	        resources.add(ServiceAdminController.class);
	        resources.add(UserController.class);
	        resources.add(SshKeyController.class);
	        resources.add(OtpController.class);

	        // Exceptions
	        resources.add(AssertionExceptionMapper.class);
	        resources.add(GenericRestInterfaceExceptionMapper.class);
	        resources.add(LoginFailedExceptionMapper.class);
	        resources.add(MisconfiguredServiceExceptionMapper.class);
	        resources.add(NoDelegationConfiguredExceptionMapper.class);
	        resources.add(NoEcpSupportExceptionMapper.class);
	        resources.add(NoHostnameConfiguredExceptionMapper.class);
	        resources.add(NoIdpForScopeExceptionMapper.class);
	        resources.add(NoIdpFoundExceptionMapper.class);
	        resources.add(NoItemFoundExceptionMapper.class);
	        resources.add(NoRegistryFoundExceptionMapper.class);
	        resources.add(NoScopedUsernameExceptionMapper.class);
	        resources.add(LoginFailedExceptionMapper.class);
	        resources.add(NoUserFoundExceptionMapper.class);
	        resources.add(PersistentIdExceptionMapper.class);
	        resources.add(RegisterExceptionMapper.class);
	        resources.add(UnauthorizedExceptionMapper.class);
	        resources.add(UserCreateExceptionMapper.class);
	        resources.add(UserNotRegisteredExceptionMapper.class);
	        resources.add(UserUpdateFailedExceptionMapper.class);
	        resources.add(ValidationExceptionMapper.class);

	        return resources;
	    }
}
