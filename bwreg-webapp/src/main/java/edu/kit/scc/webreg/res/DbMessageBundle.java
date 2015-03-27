package edu.kit.scc.webreg.res;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

public class DbMessageBundle extends ResourceBundle {

	public static final String BASE_NAME = "msg.messages";
	
	private Map<String, String> messages;
	
	public DbMessageBundle() {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		ResourceBundle rb = ResourceBundle.getBundle(BASE_NAME, locale);
		setParent(rb);
		
		messages = new HashMap<String, String>();
		messages.put("server_config", "Server Configuration");
	}

    @Override
    protected Object handleGetObject(String key) {
    	Object o = null;
    	if (messages != null) {
    		o = messages.get(key);
    	}

    	if (o == null) {
    		o = parent.getObject(key);
    	}
    	
    	return o;
    }

    @Override
    public Enumeration<String> getKeys() {
        return messages != null ? Collections.enumeration(messages.keySet()) : parent.getKeys();
    }
}
