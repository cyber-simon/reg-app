package edu.kit.scc.webreg.service.twofa;

import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface TwoFaLoginService {

	String otpLogin(String eppn, String serviceShortName, String otp, String secret, HttpServletRequest request,
			Boolean checkRegistry) throws TwoFaException, RestInterfaceException;

}
