/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.scc.webreg.service.ssh;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ldap.LdapWorker;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker to perform various operations via SSH.
 * Utilizes sshj library.
 *
 * @author bum-admin
 */
public class SshWorker {

    private static Logger logger = LoggerFactory.getLogger(LdapWorker.class);
    private Auditor auditor;

    // sending commands/passwords to ssh host's input
    private String sshHost; // host to connect to (required)
    private String sshUser; // user to connect to the host as (required)
    /*
    private String sshInput; // the input to send to the host's stream (required)
    private String ssh_input_username;
    private String ssh_input_password;
     */
    private String sshKeyFilepath; // file path to the private ssh key (optional)
    private String sshKeyPassword; // password for the private key (optional)
    private String sshKnownHosts; // filepath to the known hosts file (optional)

    SshWorker(PropertyReader prop, Auditor auditor) throws RegisterException {
        this.auditor = auditor;
        try {
            sshHost = prop.readProp("ssh_host");
            sshUser = prop.readProp("ssh_user");
            sshKeyFilepath = prop.readPropOrNull("ssh_key_filepath");
            sshKeyPassword = prop.readPropOrNull("ssh_key_password");
            sshKnownHosts = prop.readPropOrNull("ssh_known_hosts");
        } catch (PropertyReaderException e) {
            throw new RegisterException(e);
        }
    }

    public void setPassword(String uid, String password) throws RegisterException {
        // send "{ username: uid, password: password }" to STDIN of sshHost
        try {
            String response = new SshConnector().setPassword(uid, password, sshHost, sshUser,
                    sshKeyFilepath, sshKeyPassword, sshKnownHosts);
			if (!response.isBlank()) {
				if (response.contains("[STDERR]")) {
					throw new IOException(response);
				}
				logger.info("Sending password to host response: {}", response);
			}
        } catch (IOException e) {
			logger.error("IOExcetion happened in SSH Session", e);
			String message = "FAILED: Sending password over SSH to " + sshHost
				+ " as " + sshUser + " for user " + uid + ": " + e.getMessage();
//            auditor.logAction("", "SEND PASSWORD USER", uid, "Send user password failed",
//                    AuditStatus.FAIL);
            throw new RegisterException(message);
        }
    }
}
