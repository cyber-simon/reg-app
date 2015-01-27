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
package edu.kit.scc.webreg.util;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named("facesMessageGenerator")
@ApplicationScoped
public class FacesMessageGenerator {

	@Inject
	private ResourceBundleHelper resourceHelper;

	public void addErrorMessage(String messageText) {
		addResolvedErrorMessage("error_msg", messageText, false);				
	}
	
	public void addResolvedErrorMessage(String messageText) {
		addResolvedErrorMessage("error_msg", messageText, true);				
	}
	
	public void addResolvedErrorMessage(String msgName, String messageText, String detail, boolean resolveDetail) {
		addResolvedMessage(msgName, FacesMessage.SEVERITY_ERROR, messageText, detail, resolveDetail);				
	}
	
	public void addResolvedErrorMessage(String messageText, String detail, boolean resolveDetail) {
		addResolvedErrorMessage(null, messageText, detail, resolveDetail);				
	}
	
	public void addErrorMessage(String msgName, String messageText, String detail) {
		addMessage(msgName, FacesMessage.SEVERITY_ERROR, messageText, detail);				
	}
	
	public void addErrorMessage(String messageText, String detail) {
		addErrorMessage(null, messageText, detail);				
	}
	
	public void addResolvedWarningMessage(String msgName, String messageText, String detail, boolean resolveDetail) {
		addResolvedMessage(msgName, FacesMessage.SEVERITY_WARN, messageText, detail, resolveDetail);				
	}
	
	public void addResolvedWarningMessage(String messageText, String detail, boolean resolveDetail) {
		addResolvedWarningMessage(null, messageText, detail, resolveDetail);				
	}
	
	public void addResolvedInfoMessage(String msgName, String messageText, String detail, boolean resolveDetail) {
		addResolvedMessage(msgName, FacesMessage.SEVERITY_INFO, messageText, detail, resolveDetail);				
	}
	
	public void addResolvedInfoMessage(String messageText, String detail, boolean resolveDetail) {
		addResolvedInfoMessage(null, messageText, detail, resolveDetail);				
	}
	
	public void addWarningMessage(String msgName, String messageText, String detail) {
		addMessage(msgName, FacesMessage.SEVERITY_WARN, messageText, detail);				
	}
	
	public void addWarningMessage(String messageText, String detail) {
		addWarningMessage(null, messageText, detail);				
	}
	
	public void addInfoMessage(String msgName, String messageText, String detail) {
		addMessage(msgName, FacesMessage.SEVERITY_INFO, messageText, detail);				
	}
	
	public void addInfoMessage(String messageText, String detail) {
		addInfoMessage(null, messageText, detail);				
	}
	
	public void addResolvedMessage(String msgName, Severity severity, String messageText, String detail, boolean resolveDetail) {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
		if (resolveDetail)
			FacesContext.getCurrentInstance().addMessage(msgName, 
				new FacesMessage(severity, resourceHelper.resolveMessage(messageText), 
						resourceHelper.resolveMessage(detail)));
		else
			FacesContext.getCurrentInstance().addMessage(msgName, 
					new FacesMessage(severity, resourceHelper.resolveMessage(messageText), 
							detail));
	}	

	public void addMessage(String msgName, Severity severity, String messageText, String detail) {
		FacesContext.getCurrentInstance().addMessage(msgName, 
				new FacesMessage(severity, messageText, detail));				
	}	
}
