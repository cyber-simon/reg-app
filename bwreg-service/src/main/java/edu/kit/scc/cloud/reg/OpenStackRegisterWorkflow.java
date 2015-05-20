package edu.kit.scc.cloud.reg;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import edu.kit.lsdf.sns.service.PFWorker;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public class OpenStackRegisterWorkflow implements RegisterUserWorkflow,
		InfotainmentCapable, SetPasswordCapable {

	private static final Logger logger = LoggerFactory
			.getLogger(OpenStackRegisterWorkflow.class);

	@Override
	public void registerUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		String osUrl;
		if (prop.hasProp("openstack_url"))
			osUrl = prop.readPropOrNull("openstack_url");
		else
			throw new RegisterException("not configured");

		KeystoneApi keystoneApi;

		keystoneApi = ContextBuilder.newBuilder("openstack-keystone")
				.endpoint(osUrl).credentials("username", "password")
				.buildApi(KeystoneApi.class);

		Optional<? extends UserAdminApi> userAdminApiExtension = keystoneApi
				.getUserAdminApi();

		if (userAdminApiExtension.isPresent()) {
			logger.debug("UserAdminApi is present");
			
			UserAdminApi userAdminApi = userAdminApiExtension.get();
			User osUser = userAdminApi.create(user.getEppn(), "sdfghijfse48");

			registry.getRegistryValues().put("osId", osUser.getId());		

			logger.debug("created user {}", user.getEppn());
		} else {
			logger.error("UserAdminApi is *not* present");
			throw new RegisterException("UserAdminApi is *not* present");
		}

	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		PFWorker pfWorker = new PFWorker(prop, auditor);

		String userId = registry.getRegistryValues().get("osId");

	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		return false;
	}

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		if (!registry.getRegistryValues().containsKey("osId"))
			throw new RegisterException(
					"Registration is incomplete (missing osId)");

		String userId = registry.getRegistryValues().get("osId");

		Infotainment info = new Infotainment();
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);

		return info;
	}

	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password)
			throws RegisterException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deletePassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		// TODO Auto-generated method stub
		
	}
}
