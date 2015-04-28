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

import java.util.Locale;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ResourceBundleHelper {

	public String resolveMessage(String key) {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		if (facesContext == null || facesContext.getViewRoot() == null || facesContext.getViewRoot().getLocale() == null)
			return "???" + key + "???";
			
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle("edu.kit.scc.webreg.res.DbMessageBundle", locale);		

		if (bundle == null)
			return "???" + key + "???";
			
		try {
			return bundle.getString(key);
		} catch (Exception e) {
			return "???" + key + "???";
		}
	}
}
