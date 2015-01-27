/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.bean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Named("errorPageBean")
@RequestScoped
public class ErrorPageBean {

	private Exception exception;
	
    @PostConstruct
    public void init() {
		HttpServletRequest request = (HttpServletRequest)FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
		
		if (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) instanceof Exception)
			exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
		
		if (exception instanceof ServletException && exception.getCause() != null
				&& exception.getCause() instanceof Exception)
			exception = (Exception) exception.getCause();
		
	}

	public Exception getException() {
		return exception;
	}

}
