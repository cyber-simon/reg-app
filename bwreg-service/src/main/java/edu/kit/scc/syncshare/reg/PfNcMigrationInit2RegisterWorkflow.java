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
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.tools.PropertyReader;

public class PfNcMigrationInit2RegisterWorkflow extends PowerFolderRegisterWorkflow 
	implements RegisterUserWorkflow, InfotainmentCapable  {

	private static final Logger logger = LoggerFactory.getLogger(PfNcMigrationInit2RegisterWorkflow.class);

	protected ScriptingEnv scriptingEnv;

	@Override
	public void registerUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		
		super.registerUser(user, service, registry, auditor);
		
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
				}
			}
		}				
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		super.deregisterUser(user, service, registry, auditor);
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		NextcloudAnswer answer = worker.disableAccount(registry);
		if (answer.getMeta().getStatusCode() != 100) {
			logger.warn("Disabling of registry {} for user {} failed", registry.getId(), user.getEppn());
		}
	}
	
	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		
		super.reconciliation(user, service, registry, auditor);

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
	}

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user, ServiceEntity service) throws RegisterException {
		
		Infotainment info = super.getInfo(registry, user, service);
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		
		NextcloudAnswer answer = worker.loadAccount(registry);

		
		InfotainmentTreeNode root = info.getRoot();

		InfotainmentTreeNode node = new InfotainmentTreeNode("Migration User Info", root);
		if ((answer.getUser() != null) && (answer.getUser().getId() != null)) {
			if (answer.getUser().getEnabled() == null || answer.getUser().getEnabled() == false) {
				new InfotainmentTreeNode("Message", "Account is disabled!", node);
			}
			new InfotainmentTreeNode("ID", answer.getUser().getId(), node);
			new InfotainmentTreeNode("Name", answer.getUser().getDisplayName(), node);
			new InfotainmentTreeNode("E-Mail", answer.getUser().getEmail(), node);
			if (answer.getUser().getQuota() != null && answer.getUser().getQuota().getRelative() != null) {
				new InfotainmentTreeNode("Verbrauchter Platz", "" +  answer.getUser().getQuota().getRelative() + "%", node);
			}
			if (answer.getUser().getGroups() != null) {
				for (String group : answer.getUser().getGroups().getGroupList()) {
					new InfotainmentTreeNode("Gruppe", group, node);
				}
			}
		} else {
			new InfotainmentTreeNode("status", "Account not initialized yet", node);
		}

		return info;
	}
}
