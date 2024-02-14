package edu.kit.scc.webreg.bean.disco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity_;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity_;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.identity.UserProvisionerService;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DiscoveryCache implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserProvisionerService userProvisionerService;

	@Inject
	private SamlIdpMetadataService idpService;

	@Inject
	private OidcRpConfigurationService oidcRpService;

	private Long lastRefresh;
	private List<UserProvisionerCachedEntry> allEntryList;
	private List<UserProvisionerCachedEntry> initialEntryList;
	private Map<Long, UserProvisionerCachedEntry> idMap;

	@PostConstruct
	public void init() {
		logger.info("Constructing DiscoveryCache");
		allEntryList = new ArrayList<>();
		initialEntryList = new ArrayList<>();
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

		List<UserProvisionerEntity> all = userProvisionerService.findAll();
		logger.info("Building DiscoveryCache with {} entries", all.size());

		Comparator<UserProvisionerCachedEntry> comparator = new Comparator<UserProvisionerCachedEntry>() {

			@Override
			public int compare(UserProvisionerCachedEntry e1, UserProvisionerCachedEntry e2) {
				if (e1.getName() == null)
					return 0;
				return e1.getName().compareTo(e2.getName());
			}
		};

		SortedSet<UserProvisionerCachedEntry> tempAllSet = new TreeSet<>(comparator);
		SortedSet<UserProvisionerCachedEntry> tempInitalSet = new TreeSet<>(comparator);

		for (UserProvisionerEntity userProvisioner : all) {
			if (userProvisioner instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity specific = oidcRpService.findByIdWithAttrs(userProvisioner.getId(),
						OidcRpConfigurationEntity_.genericStore);
				UserProvisionerCachedEntry entry = new UserProvisionerCachedEntry();
				entry.setId(specific.getId());
				entry.setName(specific.getDisplayName());
				entry.setOrgName(specific.getGenericStore().get("org_name"));
				tempInitalSet.add(entry);
				tempAllSet.add(entry);
				idMap.put(entry.getId(), entry);
			} else if (userProvisioner instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity specific = idpService.findByIdWithAttrs(userProvisioner.getId(),
						SamlIdpMetadataEntity_.federations, SamlIdpMetadataEntity_.genericStore,
						SamlIdpMetadataEntity_.entityCategoryList);
				if (specific.getEntityCategoryList() != null && !(specific.getEntityCategoryList()
						.contains("http://refeds.org/category/hide-from-discovery"))) {
					UserProvisionerCachedEntry entry = new UserProvisionerCachedEntry();
					entry.setId(specific.getId());
					entry.setName(specific.getDisplayName());
					entry.setOrgName(specific.getOrgName());
					for (FederationEntity f : specific.getFederations()) {
						if (!f.getLoadOnButton()) {
							tempInitalSet.add(entry);
						}
					}
					tempAllSet.add(entry);
					idMap.put(entry.getId(), entry);
				}
			}
		}

		allEntryList = new ArrayList<>(tempAllSet);
		initialEntryList = new ArrayList<>(tempInitalSet);

		logger.info("Building DiscoveryCache done, initialList size {} and allList size {}", initialEntryList.size(),
				allEntryList.size());

		lastRefresh = System.currentTimeMillis();
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
}
