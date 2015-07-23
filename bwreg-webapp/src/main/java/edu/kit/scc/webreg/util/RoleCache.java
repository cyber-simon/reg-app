package edu.kit.scc.webreg.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.RoleService;

@ApplicationScoped
public class RoleCache {

	@Inject
	private Logger logger;
	
	@Inject
	private RoleService roleService;
	
	private LoadingCache<String, Long> cache;

	@PostConstruct
	public void init() {
		logger.info("Initializing roleCache");
		
		cache = CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.maximumSize(1000)
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.removalListener(removalListener)
				.build(cacheLoader);
	}
	
	public Long getIdFromRolename(String roleName) {
		try {
			return cache.get(roleName);
		} catch (ExecutionException e) {
			logger.info("Execution Exception on cache", e);
			return null;
		}
	}
	
	private CacheLoader<String, Long> cacheLoader = new CacheLoader<String, Long>() {
		public Long load(String key) {
			RoleEntity role = roleService.findByName(key);
			if (role != null)
				return role.getId();
			return null;
		}
	};
	
	private RemovalListener<String, Long> removalListener = new RemovalListener<String, Long>() {
		public void onRemoval(RemovalNotification<String, Long> removal) {
			if (logger.isTraceEnabled())
				logger.trace("Removing entry {} -> {} from roleCache ({})",
					removal.getKey(), removal.getValue(), removal.getCause());
		}
	};
}
