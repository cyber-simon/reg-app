package edu.kit.scc.syncshare.reg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.nextcloud.NextcloudAnswer;
import edu.kit.scc.nextcloud.NextcloudWorker;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.ScriptingWorkflow;
import edu.kit.scc.webreg.tools.PropertyReader;

public class PfNcMigrationRegisterWorkflow extends PowerFolderRegisterWorkflow 
	implements RegisterUserWorkflow, InfotainmentCapable, ScriptingWorkflow  {

	private static final Logger logger = LoggerFactory.getLogger(PfNcMigrationRegisterWorkflow.class);

	protected ScriptingEnv scriptingEnv;

	@Override
	public void registerUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		
		super.registerUser(user, service, registry, auditor);
		
		updateRegistry(user, service, registry, auditor);
		reconciliation(user, service, registry, auditor);

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

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
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		boolean pfUpdate = super.updateRegistry(user, service, registry, auditor);
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		Map<String, String> reconMap = new HashMap<String, String>();

		try {
			String scriptName = prop.readProp("script_name");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
			
			if (scriptEntity == null)
				throw new RegisterException("service not configured properly. script is missing.");
			
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new RegisterException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				engine.eval(scriptEntity.getScript());
			
				Invocable invocable = (Invocable) engine;
				invocable.invokeFunction("updateRegistry", scriptingEnv, reconMap, user, registry, service, auditor, prop, logger);
			}
			else {
				throw new RegisterException("unkown script type: " + scriptEntity.getScriptType());
			}
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		} catch (ScriptException e) {
			throw new RegisterException(e);
		} catch (NoSuchMethodException e) {
			throw new RegisterException(e);
		}

		Boolean change = false;
		
		for (Entry<String, String> entry : reconMap.entrySet()) {
			if (! registry.getRegistryValues().containsKey(entry.getKey())) {
				auditor.logAction("", "UPDATE USER REGISTRY", user.getEppn(), "ADD " + 
						entry.getKey() + ": " + registry.getRegistryValues().get(entry.getKey()) +
						" => " + entry.getValue()
						, AuditStatus.SUCCESS);
				registry.getRegistryValues().put(entry.getKey(), entry.getValue());
				change |= true;
			}
			else if (! registry.getRegistryValues().get(entry.getKey()).equals(entry.getValue())) {
				if (entry.getKey().equals("id")) {
					// this should not happen. It means the primary Id for the user has changed. 
					// Nextcloud saml does not support this
					logger.warn("Nextcloud User ID for user {} changes from {} to {}! This will create a new user!", 
							registry.getRegistryValues().get("id"), entry.getValue());
				}
				
				auditor.logAction("", "UPDATE USER REGISTRY", user.getEppn(), "REPLACE " + 
						entry.getKey() + ": " + registry.getRegistryValues().get(entry.getKey()) +
						" => " + entry.getValue()
						, AuditStatus.SUCCESS);
				registry.getRegistryValues().put(entry.getKey(), entry.getValue());
				change |= true;
			}
		}

		/*
		 * only reconcile registry when it's active. All other will only be given the correct attributes
		 */
		if (registry.getRegistryStatus().equals(RegistryStatus.ACTIVE))
			return (pfUpdate | change);
		else
			return false;
	}
	
	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		
		super.reconciliation(user, service, registry, auditor);

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		
		NextcloudAnswer answer = worker.loadAccount(registry);
		int statusCode = answer.getMeta().getStatusCode();
		
		if (statusCode == 404) {
			// user not found, needs to be created
			logger.info("User {} not found, needs to be created (registry id: {})", user.getId(), registry.getId());
			answer = worker.createAccount(registry);
			if (answer.getMeta().getStatusCode() != 100) {
				throw new RegisterException("Registration failed! " + answer.getMeta().getMessage());
			}

			// load the created account
			answer = worker.loadAccount(registry);
			statusCode = answer.getMeta().getStatusCode();
		}
		
		if (statusCode != 100) {
			// could not load or create account
			logger.warn("Status code {} for registry {} (userid {}) is unknown. Message: {}", 
					new Object[]{ statusCode, registry.getId(), registry.getUser().getId(), 
							answer.getMeta().getStatus()});
			throw new RegisterException("Registration failed! Could not load or create account: " + answer.getMeta().getMessage());
		}
		
		logger.debug("Account for registry {} successfully loaded", registry.getId());

		worker.updateAccount(registry);
		
		if (registry.getRegistryValues().containsKey("primaryGroup")) {
			logger.debug("Creating group {} for registry {}", 
					registry.getRegistryValues().get("primaryGroup"), registry.getId());
			NextcloudAnswer groupAnswer = worker.createGroup(registry.getRegistryValues().get("primaryGroup"));
			if (groupAnswer.getMeta().getStatusCode() == 100 || groupAnswer.getMeta().getStatusCode() == 102) {
				worker.addUserToGroup(registry, registry.getRegistryValues().get("primaryGroup"));
				logger.debug("Adding user {} to group {} for registry {}", 
						user.getId(), registry.getRegistryValues().get("primaryGroup"), registry.getId());
			}
		}	
	}

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user, ServiceEntity service) throws RegisterException {
		
		Infotainment info = super.getInfo(registry, user, service);
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		
		NextcloudAnswer answer = worker.loadAccount(registry);

		
		InfotainmentTreeNode root = info.getRoot();

		/*
		InfotainmentTreeNode node = new InfotainmentTreeNode("Status", root);
		new InfotainmentTreeNode("StatusCode", "" + answer.getMeta().getStatusCode(), node);
		new InfotainmentTreeNode("Status", answer.getMeta().getStatus(), node);
		new InfotainmentTreeNode("Message", answer.getMeta().getMessage(), node);
		 */
		
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
	
	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		super.deregisterUser(user, service, registry, auditor);
		
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		NextcloudAnswer answer = worker.disableAccount(registry);
		if (answer.getMeta().getStatusCode() != 100) {
			logger.warn("Disabling of registry {} for user {} failed", registry.getId(), user.getEppn());
			throw new RegisterException("Failed to disable account");
		}

	}

	@Override
	public void setScriptingEnv(ScriptingEnv env) {
		this.scriptingEnv = env;
	}	
}
