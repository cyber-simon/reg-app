package edu.kit.scc.webreg.service.tpl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.regapp.tpl.TemplateRenderer;

@Stateless
public class VelocityPageRendererImpl implements VelocityPageRenderer {

	@Inject
	private Logger logger;
	
	@Inject
	private TemplateRenderer renderer;

	
}
