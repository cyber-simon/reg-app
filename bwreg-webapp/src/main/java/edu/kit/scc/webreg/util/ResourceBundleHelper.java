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
import java.util.Map;
import java.util.ResourceBundle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.regapp.tpl.TemplateRenderer;

@Named
@ApplicationScoped
public class ResourceBundleHelper {

	@Inject
	private Logger logger;
	
	@Inject
	private TemplateRenderer renderer;

	public String resolveMessage(String key, Map<String, Object> rendererContext) {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		if (facesContext == null || facesContext.getViewRoot() == null || facesContext.getViewRoot().getLocale() == null)
			return "???" + key + "???";
			
		Locale locale = facesContext.getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle("edu.kit.scc.webreg.res.DbMessageBundle", locale);		

		if (bundle == null)
			return "???" + key + "???";
			
		try {
			String template = bundle.getString(key);
			String body = renderer.evaluate(template, rendererContext);
			return body;
		} catch (Exception e) {
			logger.warn("Exception while rendering resolved message: {}", e.getMessage());
			return "???" + key + "???";
		}
	}

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
			logger.warn("Exception resolving message: {}", e.getMessage());
			return "???" + key + "???";
		}
	}
}
