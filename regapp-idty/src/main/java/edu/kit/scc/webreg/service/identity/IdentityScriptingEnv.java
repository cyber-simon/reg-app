package edu.kit.scc.webreg.service.identity;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.script.AbstractScriptingEnv;
import edu.kit.scc.webreg.service.group.GroupUpdater;
import edu.kit.scc.webreg.service.project.LocalProjectCreater;
import edu.kit.scc.webreg.service.project.LocalProjectUpdater;

@ApplicationScoped
public class IdentityScriptingEnv extends AbstractScriptingEnv {

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
