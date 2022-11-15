package edu.kit.scc.webreg.service.identity;

import javax.inject.Inject;

import edu.kit.scc.webreg.service.group.GroupUpdater;
import edu.kit.scc.webreg.service.project.LocalProjectCreater;
import edu.kit.scc.webreg.service.project.LocalProjectUpdater;
import edu.kit.scc.webreg.service.saml.SamlScriptingEnv;

public class IdentityScriptingEnv extends SamlScriptingEnv {

	private static final long serialVersionUID = 1L;

	@Inject
	private LocalProjectCreater projectCreater;
	
	@Inject
	private LocalProjectUpdater projectUpdater;

	@Inject
	private GroupUpdater groupUpdater;

	public LocalProjectCreater getProjectCreater() {
		return projectCreater;
	}

	public LocalProjectUpdater getProjectUpdater() {
		return projectUpdater;
	}

	public GroupUpdater getGroupUpdater() {
		return groupUpdater;
	}
}
