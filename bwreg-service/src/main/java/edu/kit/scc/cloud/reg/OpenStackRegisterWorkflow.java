package edu.kit.scc.cloud.reg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Base64;

import java.security.SecureRandom;

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
 * Workflow Class to register OpenStack cloud users
 * 
 * @author Oleg Dulov
 */
public class OpenStackRegisterWorkflow implements RegisterUserWorkflow, InfotainmentCapable, SetPasswordCapable {

	private static final Logger logger = LoggerFactory.getLogger(OpenStackRegisterWorkflow.class);

	// generate ramdom starting PW
	private static SecureRandom random = new SecureRandom();

	// OpenStack Connection variables
	String openstack_host = "bw-cloud.org";
	String openstack_user = "osweb";
	String openstack_pass = "SSH_PASSWORD";
	String openstack_path = "/var/lib/manageos/manageos.py";
	String openstack_port = "22";
	// String defUserPasswd = "ichug3vooChecaebie9ieyingoon7ahv";
	String openstack_sudo = "";

	// Dictionaries for password generator
	private static final String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
	private static final String NUMERIC = "0123456789";
	private static final String SPECIAL_CHARS = "!@#$%^&*_=+-/";

	// Method will generate random string based on the parameters
	public static String generatePassword(int len, String dic) {
		String passwd = "";
		for (int i = 0; i < len; i++) {
			int index = random.nextInt(dic.length());
			passwd += dic.charAt(index);
		}
		return passwd;
	}

	@Override
	public void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

		// Initialize OpenStack Connection parameters
		this.initWrapper(service);

		// create random password
		String password = generatePassword(32, ALPHA + ALPHA_CAPS + NUMERIC + SPECIAL_CHARS);
		// encode PW
		byte[] encodedPassword = Base64.getEncoder().encode(password.getBytes());
		password = new String(encodedPassword);

		// read and encode entitlements
		// ADD EOS to string != NULL
		String entitlementStr = user.getAttributeStore().get("urn:oid:1.3.6.1.4.1.5923.1.1.1.7") + ";EOS";
		logger.debug("User {} has following entitlements: {}", user.getEppn(), entitlementStr);
		byte[] encodedEntitlements = Base64.getEncoder().encode(entitlementStr.getBytes());
		String entitlement = new String(encodedEntitlements);
		logger.debug("Trying to create new user {}", user.getEppn());

		// Create user
		String osUserId = this.execute("add", user.getEppn(), user.getEmail(), password, entitlement);

		if (osUserId != null && !osUserId.isEmpty()) {
			// Register user
			registry.getRegistryValues().put("osId", osUserId);

			logger.debug("created user {}", osUserId);
		} else {
			logger.info("problem with user {} (EPPN {})", osUserId, user.getEppn());
			throw new RegisterException("Cannot register user" + user.getEppn() + " with service " + service.getName());
		}

	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {

		// Initialize OpenStack Connection parameters
		this.initWrapper(service);

		logger.debug("Trying to delete user {}", user.getEppn());
		// Delete User
		String osUserId = this.execute("del", user.getEppn(), null, null, null);

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
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		// Initialize OpenStack connection parameters
		this.initWrapper(service);

		logger.debug("Trying to set service password for user {}", user.getEppn());

		String passwordRegex;
		if (prop.hasProp("password_regex"))
			passwordRegex = prop.readPropOrNull("password_regex");
		else
			passwordRegex = ".{6,}";

		String passwordRegexMessage;
		if (prop.hasProp("password_regex_message"))
			passwordRegexMessage = prop.readPropOrNull("password_regex_message");
		else
			passwordRegexMessage = "Das Passwort ist nicht komplex genug";

		if (!password.matches(passwordRegex))
			throw new RegisterException(passwordRegexMessage);

		// encode PW
		byte[] encodedPassword = Base64.getEncoder().encode(password.getBytes());
		password = new String(encodedPassword);

		// Set password
		String osUserId = this.execute("upd", user.getEppn(), null, password, null);

		logger.debug("Service password set {}", osUserId);
	}

	@Override
	public void deletePassword(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		// Initialize OpenStack connection parameters
		this.initWrapper(service);

		logger.debug("Trying to delete service password for user {}", user.getEppn());
		// Delete password
		String osUserId = this.execute("pswd", user.getEppn(), null, null, null);

		logger.debug("Service password is deleted {}", osUserId);
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

		if (prop.hasProp("openstack_sudo")) {
			if (prop.readPropOrNull("openstack_sudo").equals("true")) {
				this.openstack_sudo = "sudo ";
			}
		}

		logger.debug("OpenStack credentials are initialized {}");
	}

	private String fmt_arg(String pref, String val) {
		return val == null ? "" : " --" + pref + " " + val;
	}

	private String execute(String method, String user, String email, String password, String entitlement)
			throws RegisterException {

		String command = openstack_sudo + openstack_path + this.fmt_arg("method", method) + this.fmt_arg("user", user)
				+ this.fmt_arg("passwd", password) + this.fmt_arg("email", email)
				+ this.fmt_arg("entitlement", entitlement) + "\n";
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
