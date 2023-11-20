package edu.kit.scc.webreg.service.saml;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.script.AbstractScriptingEnv;

@ApplicationScoped
public class SamlScriptingEnv extends AbstractScriptingEnv {

	private static final long serialVersionUID = 1L;

	@Inject
	private SsoHelper ssoHelper;

	public SsoHelper getSsoHelper() {
		return ssoHelper;
	}
}
