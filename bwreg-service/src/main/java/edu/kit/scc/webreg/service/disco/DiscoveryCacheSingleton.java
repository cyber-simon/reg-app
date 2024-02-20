package edu.kit.scc.webreg.service.disco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.StatisticsDao;
import edu.kit.scc.webreg.dao.identity.UserProvisionerDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class DiscoveryCacheSingleton implements Serializable {

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
	private StatisticsDao statisticsDao;

	private Long lastRefresh;
	private List<UserProvisionerCachedEntry> allEntryList;
	private List<UserProvisionerCachedEntry> initialEntryList;
	private List<UserProvisionerCachedEntry> extraEntryList;
	private List<UserProvisionerCachedEntry> userCountEntryList;
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

	@Lock(LockType.READ)
	public boolean getCacheStale() {
		return (System.currentTimeMillis() - lastRefresh < 1000L * 60L * 60L ? false : true);
	}

	@Lock(LockType.WRITE)
	public void rebuildCache() {
		lastRefresh = 0L;
		refreshCache();
	}

	@Lock(LockType.WRITE)
	public void refreshCache() {
		if (System.currentTimeMillis() - lastRefresh < 1000L * 60L * 60L) {
			// cache is not stale yet, do not refresh
			return;
		}

		long start = System.currentTimeMillis();

		List<UserProvisionerEntity> all = userProvisionerDao.findAll();
		logger.info("Building DiscoveryCache with {} entries", all.size());

		logger.info("Getting all user provisioners sorted by user count");
		Map<Long, Long> statsMap = statisticsDao.countUsersPerIdp();
		logger.info("Map has {} entries", statsMap.size());

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
			UserProvisionerCachedEntry entry = new UserProvisionerCachedEntry();
			entry.setId(userProvisioner.getId());
			if (statsMap.containsKey(userProvisioner.getId())) {
				entry.setUserCount(statsMap.get(userProvisioner.getId()));
			}
			else {
				entry.setUserCount(0L);
			}
			
			if (userProvisioner instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity specific = oidcRpDao.fetch(userProvisioner.getId());
				if (specific.getDisplayName() == null)
					entry.setName(specific.getName());
				else
					entry.setName(specific.getDisplayName());
				entry.setOrgName(specific.getOrgName());
				if (specific.getIcon() != null)
					entry.setIconId(specific.getIcon().getId());
				if (specific.getIconLarge() != null)
					entry.setIconLargeId(specific.getIconLarge().getId());
				if (specific.getGenericStore().containsKey("show_extra")
						&& specific.getGenericStore().get("show_extra").equalsIgnoreCase("true"))
					tempExtraSet.add(entry);
				tempInitalSet.add(entry);
				tempAllSet.add(entry);
				idMap.put(entry.getId(), entry);
			} else if (userProvisioner instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity specific = idpDao.fetch(userProvisioner.getId());
				if (specific.getEntityCategoryList() != null && !(specific.getEntityCategoryList()
						.contains("http://refeds.org/category/hide-from-discovery"))) {
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
					if (specific.getGenericStore().containsKey("show_extra")
							&& specific.getGenericStore().get("show_extra").equalsIgnoreCase("true"))
						tempExtraSet.add(entry);
					tempAllSet.add(entry);
					idMap.put(entry.getId(), entry);
				}
			}
		}

		Comparator<UserProvisionerCachedEntry> userCountComparator = new Comparator<UserProvisionerCachedEntry>() {

			@Override
			public int compare(UserProvisionerCachedEntry e1, UserProvisionerCachedEntry e2) {
				return e2.getUserCount().compareTo(e1.getUserCount());
			}
		};

		userCountEntryList = new ArrayList<>(tempAllSet);
		Collections.sort(userCountEntryList, userCountComparator);
		
		allEntryList = new ArrayList<>(tempAllSet);
		initialEntryList = new ArrayList<>(tempInitalSet);
		extraEntryList = new ArrayList<>(tempExtraSet);
		long end = System.currentTimeMillis();

		logger.info("Building DiscoveryCache done, initialList size {} and allList size {} took {} ms",
				initialEntryList.size(), allEntryList.size(), (end - start));

		lastRefresh = System.currentTimeMillis();
	}

	@Lock(LockType.READ)
	public UserProvisionerCachedEntry getEntry(Long id) {
		return idMap.get(id);
	}

	@Lock(LockType.READ)
	public List<UserProvisionerCachedEntry> getAllEntryList() {
		return allEntryList;
	}

	@Lock(LockType.READ)
	public List<UserProvisionerCachedEntry> getInitialEntryList() {
		return initialEntryList;
	}

	@Lock(LockType.READ)
	public List<UserProvisionerCachedEntry> getExtraEntryList() {
		return extraEntryList;
	}

	public List<UserProvisionerCachedEntry> getUserCountEntryList() {
		return userCountEntryList;
	}
}
