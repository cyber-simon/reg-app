package edu.kit.scc.webreg.bean.tpl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.VelocityTemplateEntity;
import edu.kit.scc.webreg.service.tpl.VelocityPageRenderer;
import edu.kit.scc.webreg.service.tpl.VelocityTemplateService;

@Named("templateBean")
@RequestScoped
public class TemplateBean {

	@Inject
	private ApplicationConfig appConfig;

	@Inject 
	private HttpServletRequest request;
	
	@Inject
	private VelocityPageRenderer pageRenderer;
	
	@Inject
	private VelocityTemplateService templateService;
	
	public String getTemplated() {
		return getOrDefault(request.getServerName() + "_templated", "false");
	}
	
	public Boolean isTemplated(String name) {
		VelocityTemplateEntity tpl = templateService.findByName(name);
		return (tpl != null ? true : false);
	}
	
	public String getOrDefault(String key, String defaultString) {
		if (appConfig.getConfigValue(key) != null) {
			return appConfig.getConfigValue(key);
		}
		else {
			return defaultString;
		}	
	}
}
