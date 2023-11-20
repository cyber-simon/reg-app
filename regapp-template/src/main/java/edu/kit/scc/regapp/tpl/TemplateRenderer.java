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
package edu.kit.scc.regapp.tpl;

import java.io.StringWriter;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.exc.TemplateRenderingException;

@ApplicationScoped
public class TemplateRenderer {

	@Inject
	private Logger logger;
	
	private VelocityEngine engine;
	
	public void init() {
		
		logger.info("Initializing Velocity Engine");
		engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.init();
	}

	
	public String evaluate(String template, Map<String, Object> context)
			throws TemplateRenderingException {
		
		if (engine == null) {
			init();
		}
		
		if (template == null)
			return null;
		
		VelocityContext velocityContext = new VelocityContext(context);
		StringWriter out = new StringWriter();

		try {
			engine.evaluate(velocityContext, out, "log", template);
		} catch (ParseErrorException e) {
			throw new TemplateRenderingException(e);
		} catch (MethodInvocationException e) {
			throw new TemplateRenderingException(e);
		} catch (ResourceNotFoundException e) {
			throw new TemplateRenderingException(e);
		}
		
		return out.toString();
	}
}
