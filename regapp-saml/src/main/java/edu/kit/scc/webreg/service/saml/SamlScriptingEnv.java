package edu.kit.scc.webreg.service.saml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.script.ScriptingEnv;

@Named
@ApplicationScoped
public class SamlScriptingEnv extends ScriptingEnv {

	private static final long serialVersionUID = 1L;

	@Inject
	private SsoHelper ssoHelper;

	public SsoHelper getSsoHelper() {
		return ssoHelper;
	}
}
