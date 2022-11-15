package edu.kit.scc.syncshare.reg;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.nextcloud.NextcloudAnswer;
import edu.kit.scc.nextcloud.NextcloudWorker;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.tools.PropertyReader;

public class NextcloudProxyIdpRegisterWorkflow  implements RegisterUserWorkflow, InfotainmentCapable {

	private static final Logger logger = LoggerFactory.getLogger(NextcloudProxyIdpRegisterWorkflow.class);

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user, ServiceEntity service)
			throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		
		NextcloudAnswer answer = worker.loadAccount(registry);

		Infotainment info = new Infotainment();
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);

		info.setMessage("Account geladen");
		info.setRoot(root);
		InfotainmentTreeNode node = new InfotainmentTreeNode("Status", root);
		new InfotainmentTreeNode("StatusCode", "" + answer.getMeta().getStatusCode(), node);
		new InfotainmentTreeNode("Status", answer.getMeta().getStatus(), node);
		new InfotainmentTreeNode("Message", answer.getMeta().getMessage(), node);

		node = new InfotainmentTreeNode("User Info", root);
		if (answer.getUser() != null) {
			new InfotainmentTreeNode("ID", answer.getUser().getId(), node);
			new InfotainmentTreeNode("Name", answer.getUser().getDisplayName(), node);
			new InfotainmentTreeNode("E-Mail", answer.getUser().getEmail(), node);
			if (answer.getUser().getQuota() != null && answer.getUser().getQuota().getRelative() != null) {
				new InfotainmentTreeNode("Verbrauchter Platz", "" +  answer.getUser().getQuota().getRelative() + "%", node);
			}
			if (answer.getUser().getGroups() != null && answer.getUser().getGroups().getGroupList() != null) {
				for (String group : answer.getUser().getGroups().getGroupList()) {
					new InfotainmentTreeNode("Gruppe", group, node);
				}
			}
		}

		return info;
	}

	@Override
	public Infotainment getInfoForAdmin(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		return getInfo(registry, user, service);
	}
	
	@Override
	public void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		String idKey = "nextcloud_user_id";
		if (prop.readPropOrNull("id_key") != null) {
			idKey = prop.readPropOrNull("id_key");
		}

		String scope = "bwidm.scc.kit.edu";
		if (prop.readPropOrNull("id_scope") != null) {
			idKey = prop.readPropOrNull("id_scope");
		}

		if (! user.getGenericStore().containsKey(idKey)) {
			user.getGenericStore().put(idKey, UUID.randomUUID().toString() + "@" + scope);
			logger.debug("Generating new {} for user {}: {}", idKey, user.getId(), user.getGenericStore().get(idKey));
		}

		if (! registry.getRegistryValues().containsKey("id")) {
			registry.getRegistryValues().put("id", user.getGenericStore().get(idKey));
		}
		
		NextcloudWorker worker = new NextcloudWorker(prop);
		
		NextcloudAnswer answer = worker.loadAccount(registry);
		
		if (answer.getMeta().getStatusCode() == 100) {
			if ((answer.getUser() != null) && 
					((answer.getUser().getEnabled() == null) || (answer.getUser().getEnabled() == false))) {
				// user exists and is disabled, enable him
				NextcloudAnswer enableAnswer = worker.enableAccount(registry);
				if (enableAnswer.getMeta().getStatusCode() != 100) {
					logger.warn("Enabling of registry {} for user {} failed", registry.getId(), user.getEppn());
					throw new RegisterException("Failed to enable account");
				}
			}
		}		
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		NextcloudAnswer answer = worker.loadAccount(registry);
		
		if (answer.getMeta().getStatusCode() == 100) {
			if (answer.getUser() != null) {
				answer = worker.disableAccount(registry);
				if (answer.getMeta().getStatusCode() != 100) {
					logger.warn("Disabling of registry {} for user {} failed", registry.getId(), user.getEppn());
					throw new RegisterException("Failed to disable account");
				}
				else {
					logger.info("Account for registry {} and user {} disabled", registry.getId(), user.getEppn());
				}
			}
			else {
				logger.info("Account for registry {} and user {} does not exist in nextcloud. Deregister anyway.", registry.getId(), user.getEppn());
			}
		}
		else {
			logger.info("Account for registry {} and user {} does not exist in nextcloud. Deregister anyway.", registry.getId(), user.getEppn());
		}
	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		return false;
	}
}
