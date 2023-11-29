package edu.kit.scc.webreg.service.tpl;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class TemplateUrlStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("tpl".equals(protocol)) {
            return new TemplateUrlStreamHandler();
        }

        return null;
    }	
}
