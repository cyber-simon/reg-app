package edu.kit.scc.cloud.reg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

/**
 * Workflow Class to register bwCloud OpenStack user
 * 
 * @author Oleg Dulov
 */
public class OpenStackRegisterWorkflowClassic implements RegisterUserWorkflow, InfotainmentCapable, SetPasswordCapable {

	private static final Logger logger = LoggerFactory.getLogger(OpenStackRegisterWorkflowClassic.class);

	// OpenStack Connection variables
	String openstack_host = "os-all-one.bwcloud.scc.kit.edu";
	String openstack_user = "osweb";
	String openstack_pass = "test123";
	String openstack_path = "~/ManageOS.py";
	String defaultPass = "secretword";
	String openstack_port = "22";

	@Override
	public void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

		// Initialize OpenStack Connection parameters
		this.initWrapper(service);

		// logger.info("User: {}",user.getId());
		logger.info("User-EPPN {}", user.getEppn());

		logger.debug("try create new user");
		// Create User
		String osUserId = this.execute("add", user.getEppn(), user.getEmail(), this.defaultPass);

		if (osUserId != null && !osUserId.isEmpty()) {
			// Put User to Registry
			registry.getRegistryValues().put("osId", osUserId);

			logger.debug("created user {}", osUserId);
		} else {
			logger.info("Problem with user {}", osUserId);
			throw new RegisterException("Cannot register user under bwCloud");
		}

	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

		// Initialize OpenStack Connection parameters
		this.initWrapper(service);

		// logger.info("User: {}",user.getId());
		logger.info("User-EPPN {}", user.getEppn());

		logger.debug("try delete user: " + user.getEppn());
		// Delete User
		String osUserId = this.execute("del", user.getEppn(), "noemail", this.defaultPass);

		logger.debug("deleted user {}", osUserId);

	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		return false;
	}

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user, ServiceEntity service)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		if (!registry.getRegistryValues().containsKey("osId"))
			throw new RegisterException("Registration is incomplete (missing osId)");

		String userId = registry.getRegistryValues().get("osId");

		Infotainment info = new Infotainment();
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);

		return info;
	}

	/**
	 * SetPasswordCapable methods
	 */
	@Override
	public void setPassword(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor,
			String password) throws RegisterException {
		// Initialize OpenStack Connection parameters
		this.initWrapper(service);

		// logger.info("User: {}",user.getId());
		logger.info("User-EPPN {}", user.getEppn());

		logger.debug("try set Dienstpassword: " + user.getEppn());
		// Delete User
		String osUserId = this.execute("upd", user.getEppn(), "noemail", password);

		logger.debug("Dienstpassword set {}", osUserId);
	}

	@Override
	public void deletePassword(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		// Initialize OpenStack Connection parameters
		this.initWrapper(service);

		// logger.info("User: {}",user.getId());
		logger.info("User-EPPN {}", user.getEppn());

		logger.debug("try delete Password: " + user.getEppn());
		// Delete User
		String osUserId = this.execute("pswd", user.getEppn(), "noemail", this.defaultPass);

		logger.debug("Dienstpassword is deleted {}", osUserId);
	}

	private void initWrapper(ServiceEntity service) throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		// OpenStack Keystone URL, admin credentials
		if (prop.hasProp("openstack_host")) {
			this.openstack_host = prop.readPropOrNull("openstack_host");
		} else
			throw new RegisterException("not configured openstack_host");

		if (prop.hasProp("openstack_user")) {
			this.openstack_user = prop.readPropOrNull("openstack_user");
		} else
			throw new RegisterException("not configured openstack_user");

		if (prop.hasProp("openstack_pass")) {
			this.openstack_pass = prop.readPropOrNull("openstack_pass");
		} else
			throw new RegisterException("not configured openstack_pass");

		if (prop.hasProp("openstack_path")) {
			this.openstack_path = prop.readPropOrNull("openstack_path");
		} else
			throw new RegisterException("not configured openstack_path");

		if (prop.hasProp("openstack_port")) {
			this.openstack_port = prop.readPropOrNull("openstack_port");
		} else
			throw new RegisterException("not configured openstack_port");

		logger.debug("OpenStack Credentials are initialized {}");
	}

	private String execute(String method, String user, String email, String password) throws RegisterException {

		String command = openstack_path + " --method " + method + " --user " + user + " --passwd " + password
				+ " --email " + email + "\n";
		String line = "";

		try {
			/* Create a connection instance */

			Connection conn = new Connection(this.openstack_host, Integer.parseInt(this.openstack_port));

			/* Now connect */

			conn.connect();

			/*
			 * Authenticate. If you get an IOException saying something like
			 * "Authentication method password not supported by the server at this stage."
			 * then please check the FAQ.
			 */

			boolean isAuthenticated = conn.authenticateWithPassword(this.openstack_user, this.openstack_pass);

			if (isAuthenticated == false) {
				// throw new IOException("Authentication failed.");
				throw new RegisterException("Authentication failed.");
			}

			/* Create a session */

			Session sess = conn.openSession();

			sess.execCommand(command);

			/*
			 * This basic example does not handle stderr, which is sometimes
			 * dangerous (please read the FAQ).
			 */

			InputStream stdout = new StreamGobbler(sess.getStdout());

			BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

			line = br.readLine();

			br.close();

			/* Close this session */

			sess.close();

			/* Close the connection */

			conn.close();

		} catch (IOException e) {
			logger.error("IOExcetion happened in SSH Session", e);
			throw new RegisterException(e);
		}

		return line;

	}

}
