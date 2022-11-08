package edu.kit.scc.webreg.hook;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.script.ScriptingEnv;

public abstract class AbstractGroupServiceHook implements GroupServiceHook {

	protected ApplicationConfig appConfig;
	protected ScriptingEnv scriptingEnv;

	public AbstractGroupServiceHook() {
		super();
	}

	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void setScriptingEnv(ScriptingEnv scriptingEnv) {
		this.scriptingEnv = scriptingEnv;
	}

}