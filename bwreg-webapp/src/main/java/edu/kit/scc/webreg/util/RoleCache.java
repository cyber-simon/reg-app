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
	
	private LoadingCache<String, RoleEntity> cache;

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
	
	public RoleEntity getIdFromRolename(String roleName) {
		try {
			return cache.get(roleName);
		} catch (ExecutionException e) {
			logger.info("Execution Exception on cache", e);
			return null;
		}
	}
	
	private CacheLoader<String, RoleEntity> cacheLoader = new CacheLoader<String, RoleEntity>() {
		public RoleEntity load(String key) {
			RoleEntity role = roleService.findByName(key);
			if (role != null)
				return role;
			return null;
		}
	};
	
	private RemovalListener<String, RoleEntity> removalListener = new RemovalListener<String, RoleEntity>() {
		public void onRemoval(RemovalNotification<String, RoleEntity> removal) {
			if (logger.isTraceEnabled())
				logger.trace("Removing entry {} -> {} from roleCache ({})",
					removal.getKey(), removal.getValue(), removal.getCause());
		}
	};
}
