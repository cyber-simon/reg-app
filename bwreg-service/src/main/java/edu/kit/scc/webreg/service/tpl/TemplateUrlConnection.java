package edu.kit.scc.webreg.service.tpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.VelocityTemplateEntity;

public class TemplateUrlConnection extends URLConnection {

	private static final Logger logger = LoggerFactory.getLogger(TemplateUrlConnection.class);
	
	private VelocityTemplateService templateService;
	
	private VelocityTemplateEntity tpl;
	
	protected TemplateUrlConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		try {
			InitialContext ic = new InitialContext();
			
			templateService = (VelocityTemplateService) ic.lookup("global/bwreg/bwreg-service/VelocityTemplateServiceImpl!edu.kit.scc.webreg.service.tpl.VelocityTemplateService");

			if (logger.isTraceEnabled())
				logger.trace("Looking up template for url {}", url);
			
			tpl = templateService.findByName(url.getHost() + url.getPath());
			connected = true;
		} catch (NamingException e) {
			logger.warn("Could not load velocity template service: {}", e);
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (! connected) {
			connect();
		}
		
		if (tpl == null) {
			throw new IOException("Template not found!");
		}
		return new ByteArrayInputStream(tpl.getTemplate().getBytes("UTF-8"));
	}

	@Override
	public long getLastModified() {
		if (! connected) {
			try {
				connect();
			} catch (IOException e) {
				return -1L;
			}
		}
		
		if (tpl == null) {
			return -1L;
		}
		
		return tpl.getUpdatedAt().getTime();
	}
}
