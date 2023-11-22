package edu.kit.scc.webreg.service.ssh;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface SshLoginService extends Serializable {

	String authByUidNumberInteractive(ServiceEntity service, Integer uidNumber, HttpServletRequest request)
			throws RestInterfaceException;

	String authByUidNumber(ServiceEntity service, Integer uidNumber, HttpServletRequest request)
			throws RestInterfaceException;

	String authByUidNumberCommand(ServiceEntity service, Integer uidNumber, HttpServletRequest request)
			throws RestInterfaceException;

}
