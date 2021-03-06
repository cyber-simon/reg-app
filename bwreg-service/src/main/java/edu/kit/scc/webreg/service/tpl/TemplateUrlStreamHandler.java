package edu.kit.scc.webreg.service.tpl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class TemplateUrlStreamHandler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new TemplateUrlConnection(url);
    }
}
