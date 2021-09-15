package edu.kit.scc.webreg.script;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.service.group.GroupUpdater;
import edu.kit.scc.webreg.service.project.ProjectCreater;
import edu.kit.scc.webreg.service.project.ProjectUpdater;
import edu.kit.scc.webreg.service.saml.SsoHelper;

@Named
@ApplicationScoped
public class ScriptingEnv implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ScriptDao scriptDao;
	
	@Inject
	private UserDao userDao;

	@Inject
	private SamlUserDao samlUserDao;
	
	@Inject
	private OidcUserDao oidcUserDao;
	
	@Inject
	private GroupDao groupDao;
	
	@Inject
	private GroupUpdater groupUpdater;
	
	@Inject
	private ProjectDao projectDao;
	
	@Inject
	private ProjectCreater projectCreater;
	
	@Inject
	private ProjectUpdater projectUpdater;
	
	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private SsoHelper ssoHelper;

	public UserDao getUserDao() {
		return userDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public ServiceDao getServiceDao() {
		return serviceDao;
	}

	public RegistryDao getRegistryDao() {
		return registryDao;
	}

	public ScriptDao getScriptDao() {
		return scriptDao;
	}

	public SsoHelper getSsoHelper() {
		return ssoHelper;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

	public ProjectCreater getProjectCreater() {
		return projectCreater;
	}

	public ProjectUpdater getProjectUpdater() {
		return projectUpdater;
	}

	public SamlUserDao getSamlUserDao() {
		return samlUserDao;
	}

	public OidcUserDao getOidcUserDao() {
		return oidcUserDao;
	}

	public GroupUpdater getGroupUpdater() {
		return groupUpdater;
	}
}
