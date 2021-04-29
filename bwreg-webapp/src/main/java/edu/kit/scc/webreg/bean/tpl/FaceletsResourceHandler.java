package edu.kit.scc.webreg.bean.tpl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.application.ViewResource;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.util.BeanHelper;

public class FaceletsResourceHandler extends ResourceHandlerWrapper {

	private final static Logger logger = LoggerFactory.getLogger(FaceletsResourceHandler.class);
	
    private ResourceHandler wrapped;

    public FaceletsResourceHandler(ResourceHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewResource createViewResource(FacesContext context, final String name) {
        ViewResource resource = super.createViewResource(context, name);
        
        Object o = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (o instanceof HttpServletRequest) {
        	HttpServletRequest request = (HttpServletRequest) o;
			if (logger.isTraceEnabled())
				logger.trace("createViewResource called on hostname: {} with name {}", request.getLocalName(), name);

            TemplateBean tplBean = BeanHelper.findBean("templateBean");
            if (tplBean.getTemplated().equalsIgnoreCase("true") &&
            		tplBean.isTemplated(request.getLocalName() + name)) {
                try {
	            	URL url = new URL("tpl://" + request.getLocalName() + name);
	    			if (logger.isTraceEnabled())
	    				logger.trace("This ist a templated host and template exists! changing name to {}", url);
	                resource = new ViewResource() {
	                    @Override
	                    public URL getURL() {
	                            return url;
	                    }
	                };
                } catch (MalformedURLException e) {
                	logger.warn("Cannot build URL for template", e);
                }
            }
        }

        return resource;
    }

    @Override
    public ResourceHandler getWrapped() {
        return wrapped;
    }

}