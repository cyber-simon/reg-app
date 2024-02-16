package edu.kit.scc.webreg.service.disco;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.identity.UserProvisionerDao;
import edu.kit.scc.webreg.dao.jpa.IconCacheDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.IconCacheEntity;
import edu.kit.scc.webreg.entity.IconCacheEntity_;
import edu.kit.scc.webreg.entity.ImageDataEntity;
import edu.kit.scc.webreg.entity.ImageType;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class DiscoveryCacheService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserProvisionerDao userProvisionerDao;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private OidcRpConfigurationDao oidcRpDao;

	@Inject
	private IconCacheDao iconCacheDao;

	private Long lastRefresh;
	private List<UserProvisionerCachedEntry> allEntryList;
	private List<UserProvisionerCachedEntry> initialEntryList;
	private List<UserProvisionerCachedEntry> extraEntryList;
	private Map<Long, UserProvisionerCachedEntry> idMap;

	@PostConstruct
	public void init() {
		logger.info("Constructing DiscoveryCache");
		allEntryList = new ArrayList<>();
		initialEntryList = new ArrayList<>();
		extraEntryList = new ArrayList<>();
		idMap = new HashMap<>();

		lastRefresh = 0L;
		refreshCache();
	}

	public void rebuildCache() {
		lastRefresh = 0L;
		refreshCache();
	}

	public void refreshCache() {
		if (System.currentTimeMillis() - lastRefresh < 1000L * 60L * 60L) {
			// cache is not stale yet, do not refresh
			return;
		}

		long start = System.currentTimeMillis();

		List<UserProvisionerEntity> all = userProvisionerDao.findAll();
		logger.info("Building DiscoveryCache with {} entries", all.size());

		Comparator<UserProvisionerCachedEntry> comparator = new Comparator<UserProvisionerCachedEntry>() {

			@Override
			public int compare(UserProvisionerCachedEntry e1, UserProvisionerCachedEntry e2) {
				return e1.getName().compareTo(e2.getName());
			}
		};

		SortedSet<UserProvisionerCachedEntry> tempAllSet = new TreeSet<>(comparator);
		SortedSet<UserProvisionerCachedEntry> tempInitalSet = new TreeSet<>(comparator);
		SortedSet<UserProvisionerCachedEntry> tempExtraSet = new TreeSet<>(comparator);

		for (UserProvisionerEntity userProvisioner : all) {
			if (userProvisioner instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity specific = oidcRpDao.fetch(userProvisioner.getId());
				UserProvisionerCachedEntry entry = new UserProvisionerCachedEntry();
				entry.setId(specific.getId());
				if (specific.getDisplayName() == null)
					entry.setName(specific.getName());
				else
					entry.setName(specific.getDisplayName());
				entry.setOrgName(specific.getOrgName());
				if (specific.getIcon() != null)
					entry.setIconId(specific.getIcon().getId());
				if (specific.getIconLarge() != null)
					entry.setIconLargeId(specific.getIconLarge().getId());
				if (specific.getGenericStore().containsKey("show_extra") && specific.getGenericStore().get("show_extra").equalsIgnoreCase("true"))
					tempExtraSet.add(entry);
				tempInitalSet.add(entry);
				tempAllSet.add(entry);
				idMap.put(entry.getId(), entry);
			} else if (userProvisioner instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity specific = idpDao.fetch(userProvisioner.getId());
				if (specific.getEntityCategoryList() != null && !(specific.getEntityCategoryList()
						.contains("http://refeds.org/category/hide-from-discovery"))) {
					UserProvisionerCachedEntry entry = new UserProvisionerCachedEntry();
					entry.setId(specific.getId());
					if (specific.getDisplayName() == null)
						entry.setName(specific.getOrgName());
					else
						entry.setName(specific.getDisplayName());
					entry.setOrgName(specific.getOrgName());
					if (specific.getIcon() != null)
						entry.setIconId(specific.getIcon().getId());
					if (specific.getIconLarge() != null)
						entry.setIconLargeId(specific.getIconLarge().getId());
					for (FederationEntity f : specific.getFederations()) {
						if (!f.getLoadOnButton()) {
							tempInitalSet.add(entry);
						}
					}
					if (specific.getGenericStore().containsKey("show_extra") && specific.getGenericStore().get("show_extra").equalsIgnoreCase("true"))
						tempExtraSet.add(entry);
					tempAllSet.add(entry);
					idMap.put(entry.getId(), entry);
				}
			}
		}

		allEntryList = new ArrayList<>(tempAllSet);
		initialEntryList = new ArrayList<>(tempInitalSet);
		extraEntryList = new ArrayList<>(tempExtraSet);
		long end = System.currentTimeMillis();

		logger.info("Building DiscoveryCache done, initialList size {} and allList size {} took {} ms",
				initialEntryList.size(), allEntryList.size(), (end - start));

		lastRefresh = System.currentTimeMillis();
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
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			try {
				httpClient.execute(new HttpGet(icon.getUrl()), response -> {
					if (response.getCode() == 200) {
						// max 200kb
						final byte[] body = EntityUtils.toByteArray(response.getEntity(), 200 * 1024);
						final String mimeType = ContentType.parse(response.getEntity().getContentType()).getMimeType();
						setIconData(body, icon, mimeType);
						icon.setValidUntil(new Date(System.currentTimeMillis() + (30L * 60L * 1000L)));
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
		return idMap.get(id);
	}

	public List<UserProvisionerCachedEntry> getAllEntryList() {
		return allEntryList;
	}

	public List<UserProvisionerCachedEntry> getInitialEntryList() {
		return initialEntryList;
	}

	public List<UserProvisionerCachedEntry> getExtraEntryList() {
		return extraEntryList;
	}
}
