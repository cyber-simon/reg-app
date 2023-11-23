package edu.kit.scc.webreg.script;

import java.io.Serializable;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;

public class AbstractScriptingEnv implements Serializable {

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
	private SerialDao serialDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;

	@Inject
	private ProjectDao projectDao;
	
	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private RegistryDao registryDao;
	
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

	public ProjectDao getProjectDao() {
		return projectDao;
	}

	public SamlUserDao getSamlUserDao() {
		return samlUserDao;
	}

	public OidcUserDao getOidcUserDao() {
		return oidcUserDao;
	}

	public SerialDao getSerialDao() {
		return serialDao;
	}

	public ServiceGroupFlagDao getGroupFlagDao() {
		return groupFlagDao;
	}
	
	public Object selectFromCDI(String className) throws ClassNotFoundException {
		return CDI.current().select(Class.forName(className)).get();
	}
}
