package edu.kit.scc.webreg.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.ServiceService;

@ApplicationScoped
@Named
public class ServiceCache {

	@Inject
	private Logger logger;
	
	@Inject
	private ServiceService serviceService;
	
	private LoadingCache<Long, ServiceEntity> cache;

	@PostConstruct
	public void init() {
		logger.info("Initializing serviceCache");
		
		cache = CacheBuilder.newBuilder()
				.concurrencyLevel(4)
				.maximumSize(100)
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.removalListener(removalListener)
				.build(cacheLoader);
	}
	
	public ServiceEntity getServiceFromId(Long key) {
		try {
			return cache.get(key);
		} catch (ExecutionException e) {
			logger.info("Execution Exception on cache", e);
			return null;
		}
	}
	
	private CacheLoader<Long, ServiceEntity> cacheLoader = new CacheLoader<Long, ServiceEntity>() {
		public ServiceEntity load(Long key) {
			ServiceEntity service = serviceService.findByIdWithServiceProps(key);
			return service;
		}
	};
	
	private RemovalListener<Long, ServiceEntity> removalListener = new RemovalListener<Long, ServiceEntity>() {
		public void onRemoval(RemovalNotification<Long, ServiceEntity> removal) {
			if (removal.getValue() != null && logger.isTraceEnabled())
				logger.trace("Removing entry {} -> {} from serviceCache ({})",
					removal.getKey(), removal.getValue().getName(), removal.getCause());
		}
	};
}
