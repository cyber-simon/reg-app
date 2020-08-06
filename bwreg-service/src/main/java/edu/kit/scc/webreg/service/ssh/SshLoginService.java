package edu.kit.scc.webreg.service.ssh;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface SshLoginService extends Serializable {

	String authByUidNumberInteractive(ServiceEntity service, Long uidNumber, HttpServletRequest request)
			throws IOException, RestInterfaceException;

	String authByUidNumber(ServiceEntity service, Long uidNumber, HttpServletRequest request)
			throws IOException, RestInterfaceException;

	String authByUidNumberCommand(ServiceEntity service, Long uidNumber, HttpServletRequest request)
			throws IOException, RestInterfaceException;

}
