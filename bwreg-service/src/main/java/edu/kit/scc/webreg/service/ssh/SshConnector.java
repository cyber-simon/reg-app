/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.scc.webreg.service.ssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
/**
 *
 * @author bum-admin
 */
public class SshConnector {

    private final SSHClient ssh = new SSHClient();

    private String output;


    /**
    * escapeString()
    *
    * Escape a given String to make it safe to be printed or stored.
    *
    * @param s The input String.
    * @return The escaped output String.
    **/
    public String escapeString(String s){
      return s.replace("\\", "\\\\")
              .replace("\t", "\\t")
              .replace("\b", "\\b")
              .replace("\n", "\\n")
              .replace("\r", "\\r")
              .replace("\f", "\\f")
              .replace("\'", "\\'")
              .replace("\"", "\\\"");
    }

    /**
     * sendInput()
     *
     * Send command as input to the host's InputStream.
     *
     * @param command The input string to be streamed into the hosts InputStream.
     * @param host The host address.
     * @param user The user to connect to the host as.
     * @param privateKeyFilepath The path to the private ssh key to authenticate with.
     * @param privateKeyPassword The password for the private ssh key at privateKeyFilepath.
     * @param knownHostsFilepath The path to the file containing known hosts to verify host with.
     * @return Any output or errors from the host as a single string.
     * @throws java.io.IOException
    **/
    public String sendInput(String command, String host, String user, String privateKeyFilepath, String privateKeyPassword, String knownHostsFilepath) throws IOException{

        output = "";
        if (privateKeyFilepath == null || privateKeyFilepath.isBlank())
        {
            if (knownHostsFilepath == null || knownHostsFilepath.isBlank())
            {
                ssh.loadKnownHosts();
            }
            else
            {
                ssh.loadKnownHosts(new File(knownHostsFilepath));
            }

            ssh.connect(host);

            ssh.authPublickey(user);
        }
        else
        {
            if (knownHostsFilepath == null || knownHostsFilepath.isBlank())
            {
                ssh.loadKnownHosts(new File(privateKeyFilepath.substring(0,privateKeyFilepath.lastIndexOf("/")) + "/known_hosts"));
            }
            else
            {
                ssh.loadKnownHosts(new File(knownHostsFilepath));
            }

            ssh.connect(host);

            if (privateKeyPassword == null || privateKeyPassword.isBlank())
            {
                ssh.authPublickey(user, privateKeyFilepath);
            }
            else
            {
                ssh.authPublickey(user, ssh.loadKeys(privateKeyFilepath, privateKeyPassword));
            }
        }

        try (Session session = ssh.startSession(); Shell shl = session.startShell()) {
            // thread to read the InputStream (host's STDOUT)
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        InputStream in = shl.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = br.readLine()) != null) {
                            output += "[STDOUT] " + line + "\n";
                        }
                    } catch (Exception e) {
                        output += e.getMessage();
                    }
                }
            };
            t.start();
            // thread to read the ErrorStream (host's STDERR)
            Thread err_t = new Thread() {
                @Override
                public void run() {
                    try {
                        InputStream in = shl.getErrorStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String line;
                        while ((line = br.readLine()) != null) {
                            output += "[STDERR] " + line + "\n";
                        }
                    } catch (Exception e) {
                        output += e.getMessage();
                    }
                }
            };
            err_t.start();

            byte[] data = command.getBytes();
            try (OutputStream out = shl.getOutputStream()) {
                out.write(data);
                out.flush();
            }
            shl.join(5, TimeUnit.SECONDS);
        } finally {
            ssh.disconnect();
            return output;
        }
    }

    /**
     * setPassword()
     *
     * Send a JSON string of the format '{ "username" : userName , "password" : userPassword }' to the host's InputStream.
     *
     * @param userName The name od the user.
     * @param userPassword The password for the given userName.
     * @param host The host address.
     * @param user The user to connect to the host as.
     * @param privateKeyFilepath The path to the private ssh key to authenticate with.
     * @param privateKeyPassword The password for the private ssh key at privateKeyFilepath.
     * @param knownHostsFilepath The path to the file containing known hosts to verify host with.
     * @return Any output or errors from the host as a single string.
     * @throws java.io.IOException
    **/
    public String setPassword (String userName, String userPassword, String host, String user, String privateKeyFilepath, String privateKeyPassword, String knownHostsFilepath) throws IOException
    {
        return sendInput("{ \"username\" : \"" + escapeString(userName) + "\" , \"password\" : \"" + escapeString(userPassword) + "\" }",
                host, user, privateKeyFilepath, privateKeyPassword, knownHostsFilepath);
    }
}
