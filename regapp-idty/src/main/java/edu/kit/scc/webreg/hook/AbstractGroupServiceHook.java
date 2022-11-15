package edu.kit.scc.webreg.hook;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;

public abstract class AbstractGroupServiceHook implements GroupServiceHook {

	protected ApplicationConfig appConfig;
	protected IdentityScriptingEnv scriptingEnv;

	public AbstractGroupServiceHook() {
		super();
	}

	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void setScriptingEnv(IdentityScriptingEnv scriptingEnv) {
		this.scriptingEnv = scriptingEnv;
	}

}