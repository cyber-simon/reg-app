package edu.kit.scc.webreg.service.reg.ldap;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.TemplateRenderingException;
import edu.kit.scc.webreg.service.mail.TemplateRenderer;
import edu.kit.scc.webreg.service.ssh.SshWorker;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This workflow will evaluate a Velocity template (ssh_input_template)
 * and transmit the result to the STDIN of a specified SSH host (see SshWorker.java).
 *
 * @author Michael Burgardt
 */
public class ScriptedLdapSshRegisterWorkflow extends ScriptedLdapRegisterWorkflow {

	@Override
	public void setPassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor, String password) throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		Map<String, String> regMap = registry.getRegistryValues();

		Map<String, Object> context = new HashMap<>(5);
		context.put("password", password);
		context.put("registry", registry);
		context.put("user", user);
		context.put("service", service);
		String localUid = regMap.get("localUid");
		context.put("uid", localUid);

		String template = "";
		try {
			InitialContext ic = new InitialContext();
			TemplateRenderer renderer = (TemplateRenderer) ic.lookup("global/bwreg/bwreg-service/TemplateRenderer!edu.kit.scc.webreg.service.mail.TemplateRenderer");
			template = prop.readProp("ssh_input_template");
			String input = renderer.evaluate(template, context);

			SshWorker sshWorker = new SshWorker(prop, auditor);
			sshWorker.sendInput(input);
		} catch (NamingException ex) {
			logger.error("Could not retrieve TemplateRenderer");
			auditor.logAction("", "SSH SET USER PASSWORD", localUid, "Setting user password failed",
					AuditStatus.FAIL);
			throw new RegisterException(ex);
		} catch (TemplateRenderingException ex) {
			logger.error("Could not evaluate provided template: " + (template.isBlank() ? "template is empty" : template));
			auditor.logAction("", "SSH SET USER PASSWORD", localUid, "Setting user password failed",
					AuditStatus.FAIL);
			throw new RegisterException(ex);
		} catch (PropertyReaderException ex) {
			logger.error("ScriptedLdapSshRegisterWorkflow not configured corresctly: ssh_input_template is missing");
			auditor.logAction("", "SSH SET USER PASSWORD", localUid, "Setting user password failed",
					AuditStatus.FAIL);
			throw new RegisterException(ex);
		}
	}

	@Override
	public void deletePassword(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		throw new RegisterException("Deleting passwords via ssh is not supported!");
	}
}
