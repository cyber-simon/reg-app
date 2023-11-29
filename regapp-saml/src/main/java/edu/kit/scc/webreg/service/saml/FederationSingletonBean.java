package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FederationSingletonBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private FederationDao federationDao;

	private Map<FederationEntity, List<SamlIdpMetadataEntity>> federationCache;
	private List<FederationEntity> sortedFederationList;
	private Map<String, SamlIdpMetadataEntity> idpMap;

	private Long lastRefresh;

	@PostConstruct
	public void init() {
		logger.info("Constructing federation cache");
		federationCache = new HashMap<FederationEntity, List<SamlIdpMetadataEntity>>();
		idpMap = new HashMap<String, SamlIdpMetadataEntity>();
		lastRefresh = 0L;
		refreshCache();
	}

	public void refreshCache() {
		if (System.currentTimeMillis() - lastRefresh > 1000L * 60L * 60L) {

			List<FederationEntity> tempFederationList = federationDao.findAll();
			sortedFederationList = new ArrayList<FederationEntity>();

			idpMap.clear();

			for (FederationEntity federation : tempFederationList) {
				logger.info("Loading federation {} ({})", federation.getId(), federation.getEntityId());

				List<SamlIdpMetadataEntity> unfilteredIdpList = idpDao.findAllByFederationOrderByOrgname(federation);
				List<SamlIdpMetadataEntity> idpList = unfilteredIdpList.stream().filter(idp -> {
					if (idp.getEntityCategoryList() != null && (!idp.getEntityCategoryList()
							.contains("http://refeds.org/category/hide-from-discovery")))
						return true;
					else
						return false;
				}).toList();

				federationCache.put(federation, idpList);
				for (SamlIdpMetadataEntity idp : idpList) {
					idpMap.put(idp.getEntityId(), idp);
				}

				if (idpList.size() > 0) {
					sortedFederationList.add(federation);
				}
			}

			lastRefresh = System.currentTimeMillis();
		}
	}

	public List<FederationEntity> getFederationList() {
		return sortedFederationList;
	}

	public List<SamlIdpMetadataEntity> getIdpList(FederationEntity federationEntity) {
		return federationCache.get(federationEntity);
	}

	public List<SamlIdpMetadataEntity> getFilteredIdpList(ScriptEntity scriptEntity) {
		refreshCache();

		ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

		List<SamlIdpMetadataEntity> tempList = new ArrayList<SamlIdpMetadataEntity>(idpMap.values());
		Collections.sort(tempList, idpOrgComparator);

		if (engine == null) {
			logger.warn("No engine set for script {}. Returning all IDPs", scriptEntity.getName());
			return tempList;
		}

		try {
			List<SamlIdpMetadataEntity> targetList = new ArrayList<SamlIdpMetadataEntity>();

			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;

			invocable.invokeFunction("filterIdps", tempList, targetList, logger);

			Collections.sort(targetList, idpOrgComparator);

			return targetList;
		} catch (ScriptException e) {
			logger.warn("Script execution failed.", e);
			return tempList;
		} catch (NoSuchMethodException e) {
			logger.info("No filterIdps method in script. returning all Idps");
			return tempList;
		}
	}

	public List<SamlIdpMetadataEntity> getAllIdpList() {
		refreshCache();

		List<SamlIdpMetadataEntity> tempList = new ArrayList<SamlIdpMetadataEntity>(idpMap.values());
		Collections.sort(tempList, idpOrgComparator);
		return tempList;
	}

	private Comparator<SamlIdpMetadataEntity> idpOrgComparator = new Comparator<SamlIdpMetadataEntity>() {

		@Override
		public int compare(SamlIdpMetadataEntity idp1, SamlIdpMetadataEntity idp2) {
			if (idp1 != null && idp1.getOrgName() != null && idp2 != null && idp2.getOrgName() != null)
				return idp1.getOrgName().compareTo(idp2.getOrgName());
			else
				return 0;
		}

	};
}
