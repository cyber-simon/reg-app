package edu.kit.scc.webreg.script;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
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
	private GroupDao groupDao;
	
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

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public ServiceDao getServiceDao() {
		return serviceDao;
	}

	public void setServiceDao(ServiceDao serviceDao) {
		this.serviceDao = serviceDao;
	}

	public RegistryDao getRegistryDao() {
		return registryDao;
	}

	public void setRegistryDao(RegistryDao registryDao) {
		this.registryDao = registryDao;
	}

	public ScriptDao getScriptDao() {
		return scriptDao;
	}

	public void setScriptDao(ScriptDao scriptDao) {
		this.scriptDao = scriptDao;
	}

	public SsoHelper getSsoHelper() {
		return ssoHelper;
	}

	public void setSsoHelper(SsoHelper ssoHelper) {
		this.ssoHelper = ssoHelper;
	}

	public ProjectDao getProjectDao() {
		return projectDao;
	}

	public void setProjectDao(ProjectDao projectDao) {
		this.projectDao = projectDao;
	}

	public ProjectCreater getProjectCreater() {
		return projectCreater;
	}

	public void setProjectCreater(ProjectCreater projectCreater) {
		this.projectCreater = projectCreater;
	}

	public ProjectUpdater getProjectUpdater() {
		return projectUpdater;
	}

	public void setProjectUpdater(ProjectUpdater projectUpdater) {
		this.projectUpdater = projectUpdater;
	}
	
}
