package edu.kit.scc.webreg.service.disco;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofMilliseconds(5000L)) .build();
			CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
			try {
				httpClient.execute(new HttpGet(icon.getUrl()), response -> {
					if (response.getCode() == 200) {
						// max 200kb
						final byte[] body = EntityUtils.toByteArray(response.getEntity(), 200 * 1024);
						final String mimeType = ContentType.parse(response.getEntity().getContentType()).getMimeType();
						setIconData(body, icon, mimeType);
						icon.setValidUntil(new Date(System.currentTimeMillis() + (30L * 60L * 1000L)));
					}
					else {
						logger.debug("Refreshing icon {} from url {} failed: {}", icon.getId(), icon.getUrl(), response.getCode());
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

	public List<UserProvisionerCachedEntry> getAllEntryList() {
		return singleton.getAllEntryList();
	}

	public List<UserProvisionerCachedEntry> getUserCountEntryList() {
		return singleton.getUserCountEntryList();
	}

	public List<UserProvisionerCachedEntry> getInitialEntryList() {
		return singleton.getInitialEntryList();
	}

	public List<UserProvisionerCachedEntry> getExtraEntryList() {
		return singleton.getExtraEntryList();
	}

}
