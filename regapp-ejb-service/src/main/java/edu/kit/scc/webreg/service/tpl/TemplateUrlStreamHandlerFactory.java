package edu.kit.scc.webreg.service.tpl;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class TemplateUrlStreamHandlerFactory implements URLStreamHandlerFactory {

	private URLStreamHandlerFactory originalFactory;
	
	public TemplateUrlStreamHandlerFactory(URLStreamHandlerFactory originalFactory) {
		this.originalFactory = originalFactory;
	}
	
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("tpl".equals(protocol)) {
            return new TemplateUrlStreamHandler();
        }

        return originalFactory.createURLStreamHandler(protocol);
    }	
}
