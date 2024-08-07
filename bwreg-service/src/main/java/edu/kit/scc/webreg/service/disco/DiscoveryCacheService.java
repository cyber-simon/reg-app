package edu.kit.scc.webreg.service.disco;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.IconCacheDao;
import edu.kit.scc.webreg.entity.IconCacheEntity;
import edu.kit.scc.webreg.entity.IconCacheEntity_;
import edu.kit.scc.webreg.entity.ImageDataEntity;
import edu.kit.scc.webreg.entity.ImageType;
import edu.kit.scc.webreg.entity.ScriptEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class DiscoveryCacheService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private DiscoveryCacheSingleton singleton;

	@Inject
	private IconCacheDao iconCacheDao;

	public void refreshCache() {
		if (singleton.getCacheStale())
			singleton.refreshCache();
	}

	public IconCacheEntity getIcon(Long id) {
		return iconCacheDao.find(equal(IconCacheEntity_.id, id), IconCacheEntity_.imageData);
	}

	public IconCacheEntity getIconAsync(Long id) {
		final IconCacheEntity icon = getIcon(id);
		if (icon.getValidUntil() == null || icon.getValidUntil().before(new Date())) {
			// TODO instead of fetching, send JMS to do the update async
			fetchIcon(icon);
		}

		return icon;
	}

	public IconCacheEntity getIconSync(Long id) {
		final IconCacheEntity icon = getIcon(id);

		if (icon.getValidUntil() == null || icon.getValidUntil().before(new Date())) {
			// refresh icon
			fetchIcon(icon);
		}

		return icon;
	}

	private void fetchIcon(IconCacheEntity icon) {
		// If we fail, don't try again for some time
		icon.setValidUntil(new Date(System.currentTimeMillis() + (30L * 60L * 1000L)));

		if (icon.getUrl() != null) {
			logger.debug("Refreshing icon {} from url {}", icon.getId(), icon.getUrl());
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(Timeout.ofMilliseconds(5000L)).build();
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
			try {
				httpClient.execute(new HttpGet(icon.getUrl()), response -> {
					if (response.getCode() == 200) {
						// max 200kb
						final byte[] body = EntityUtils.toByteArray(response.getEntity(), 200 * 1024);
						final String mimeType = ContentType.parse(response.getEntity().getContentType()).getMimeType();
						setIconData(body, icon, mimeType);
						icon.setValidUntil(new Date(System.currentTimeMillis() + (30L * 60L * 1000L)));
					} else {
						logger.debug("Refreshing icon {} from url {} failed: {}", icon.getId(), icon.getUrl(),
								response.getCode());
					}
					return response;
				});
			} catch (IOException e) {
				logger.info("Refreshing icon {} from url {}: IOException: {}", icon.getId(), icon.getUrl(),
						e.getMessage());
			}
		}
	}

	private void setIconData(byte[] body, IconCacheEntity icon, String mimeType) throws IOException {

		if (mimeType == null) {
			icon.setImageType(ImageType.NOT_SUPPORTED);
		} else if (mimeType.equals("image/png")) {
			icon.setImageType(ImageType.PNG);
		} else if (mimeType.equals("image/jpeg")) {
			icon.setImageType(ImageType.JPEG);
		} else if (mimeType.equals("image/svg+xml")) {
			icon.setImageType(ImageType.SVG);
		} else {
			icon.setImageType(ImageType.NOT_SUPPORTED);
		}

		if (icon.getImageData() == null) {
			icon.setImageData(new ImageDataEntity());
		}
		icon.getImageData().setData(body);
	}

	public UserProvisionerCachedEntry getEntry(Long id) {
		return singleton.getEntry(id);
	}

	public List<UserProvisionerCachedEntry> getAllEntryList(List<ScriptEntity> filterScriptList) {
		return filterAllEntries(filterScriptList, singleton.getAllEntryList());
	}

	public List<UserProvisionerCachedEntry> getUserCountEntryList(List<ScriptEntity> filterScriptList) {
		return filterAllEntries(filterScriptList, singleton.getUserCountEntryList());
	}

	public List<UserProvisionerCachedEntry> getExtraEntryList(List<ScriptEntity> filterScriptList) {
		return filterAllEntries(filterScriptList, singleton.getExtraEntryList());
	}

	private List<UserProvisionerCachedEntry> filterAllEntries(List<ScriptEntity> filterScriptList,
			List<UserProvisionerCachedEntry> entryList) {
		if (filterScriptList != null && filterScriptList.size() > 0) {
			Comparator<UserProvisionerCachedEntry> comparator = new Comparator<UserProvisionerCachedEntry>() {

				@Override
				public int compare(UserProvisionerCachedEntry e1, UserProvisionerCachedEntry e2) {
					if (e1.getDisplayName() != null)
						return e1.getDisplayName().compareTo(e2.getDisplayName());
					else 
						return 0;
				}
			};

			Set<UserProvisionerCachedEntry> returnList = new TreeSet<>(comparator);
			for (ScriptEntity script : filterScriptList) {
				returnList.addAll(filterEntries(script, entryList));
			}
			return new ArrayList<>(returnList);
		} else
			return entryList;
	}

	private List<UserProvisionerCachedEntry> filterEntries(ScriptEntity scriptEntity,
			List<UserProvisionerCachedEntry> entryList) {
		ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

		if (engine == null) {
			logger.warn("No engine set for script {}. Returning all IDPs", scriptEntity.getName());
			return entryList;
		}

		try {
			List<UserProvisionerCachedEntry> targetList = new ArrayList<>();

			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;

			// If one of two succeeds, use target list, else return all
			Boolean success = false;

			try {
				List<UserProvisionerCachedEntry> idpList = new ArrayList<>();
				invocable.invokeFunction("filterIdps", entryList, idpList, logger);
				targetList.addAll(idpList);
				success = true;
			} catch (NoSuchMethodException e) {
			}

			try {
				List<UserProvisionerCachedEntry> opList = new ArrayList<>();
				invocable.invokeFunction("filterOps", entryList, opList, logger);
				targetList.addAll(opList);
				success = true;
			} catch (NoSuchMethodException e) {
			}

			if (success)
				return targetList;
			else
				return entryList;
		} catch (ScriptException e) {
			logger.warn("Script execution failed.", e);
			return entryList;
		}
	}
}
