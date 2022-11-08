package edu.kit.scc.webreg.session;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;

@RequestScoped
public class HttpRequestContext {

	private HttpServletRequest httpServletRequest;

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}
}
