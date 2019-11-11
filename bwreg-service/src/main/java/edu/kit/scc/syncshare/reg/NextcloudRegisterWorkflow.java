package edu.kit.scc.syncshare.reg;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
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
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public class NextcloudRegisterWorkflow  implements RegisterUserWorkflow, InfotainmentCapable {

	private static final Logger logger = LoggerFactory.getLogger(NextcloudRegisterWorkflow.class);

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
			if (answer.getUser().getQuota() != null) {
				new InfotainmentTreeNode("Verbrauchter Platz", "" +  answer.getUser().getQuota().getRelative() + "%", node);
			}
		}

		return info;
	}

	@Override
	public void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		
		updateRegistry(user, service, registry, auditor);
		reconciliation(user, service, registry, auditor);
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		NextcloudWorker worker = new NextcloudWorker(prop);
		
		NextcloudAnswer answer = worker.loadAccount(registry);
		int statusCode = answer.getMeta().getStatusCode();
		
		if (statusCode == 404) {
			// user not found, needs to be created
			
		} else if (statusCode == 100) {
			// user found, ok
		} else {
			// unknown status code
			logger.warn("Status code {} for registry {} (userid {}) is unknown. Message: {}", 
					new Object[]{ statusCode, registry.getId(), registry.getUser().getId(), 
							answer.getMeta().getStatus()});
		}
	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		String tplId = prop.readPropOrNull("tpl_id");
		String id;
		if (tplId != null) {
			id = evalTemplate(tplId, user);
		} 
		else {
			id = user.getEppn();
		}

		if (! registry.getRegistryValues().containsKey("id")) {
			registry.getRegistryValues().put("id", id);
		} else {
			if (! registry.getRegistryValues().get("id").equals(id)) {
				// this should not happen. It means the primary Id for the user has changed. 
				// Nextcloud saml does not support this
				logger.warn("Nextcloud User ID for user {} would change from {} to {}!", registry.getRegistryValues().get("id"), id);
			}
		}

		return false;
	}
	
	protected String evalTemplate(String template, UserEntity user) 
			throws RegisterException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.init();
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", user);
		VelocityContext velocityContext = new VelocityContext(context);
		StringWriter out = new StringWriter();

		try {
			engine.evaluate(velocityContext, out, "log", template);
			
			return out.toString();
		} catch (ParseErrorException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		} catch (MethodInvocationException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		} catch (ResourceNotFoundException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		}
	}

}
