package edu.kit.scc.webreg.res;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import edu.kit.scc.webreg.dao.TextPropertyDao;
import edu.kit.scc.webreg.entity.TextPropertyEntity;
import edu.kit.scc.webreg.util.BeanHelper;

public class DbMessageBundle extends ResourceBundle {

	public static final Logger logger = LoggerFactory.getLogger(DbMessageBundle.class);
	public static final String BASE_NAME = "msg.messages";
	
	private LoadingCache<String, BundleEntry> messages;
	
	private Locale locale;
	
	private TextPropertyDao textPropertyDao;
	
	public DbMessageBundle() {
		locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		
		ResourceBundle rb = ResourceBundle.getBundle(BASE_NAME, locale);
		setParent(rb);
		
		textPropertyDao = BeanHelper.findBean("jpaTextPropertyDao");
		
		messages = CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.maximumSize(10000)
				.expireAfterWrite(30, TimeUnit.SECONDS)
				.removalListener(removalListener)
				.build(cacheLoader);
	}

    @Override
    protected Object handleGetObject(String key) {
    	Object o = null;

    	if (messages != null) {
    		BundleEntry be = messages.getUnchecked(key);
    		if (be != null)
    			o = be.getValue();
    	}

    	if (o == null) {
    		o = parent.getObject(key);
    	}
    	
    	return o;
    }

    @Override
    public Enumeration<String> getKeys() {
    	Set<String> rets = messages.asMap().keySet();
    	rets.addAll(parent.keySet());
        return Collections.enumeration(rets);
    }
    
    private CacheLoader<String, BundleEntry> cacheLoader = new CacheLoader<String, BundleEntry>() {
		public BundleEntry load(String key) {
			BundleEntry be = new BundleEntry();
			TextPropertyEntity tpe = textPropertyDao
					.findByKeyAndLang(key, locale.getLanguage());
			if (tpe != null)
				be.setValue(tpe.getValue());
			return be;
		}
	};
    
	private RemovalListener<String, BundleEntry> removalListener = new RemovalListener<String, BundleEntry>() {
		public void onRemoval(RemovalNotification<String, BundleEntry> removal) {
			if (logger.isTraceEnabled() && removal.getValue() != null)
				logger.trace("Removing entry {} -> {} from messageCache ({})",
					removal.getKey(), removal.getValue().getValue(), removal.getCause());
		}
	};    
}
