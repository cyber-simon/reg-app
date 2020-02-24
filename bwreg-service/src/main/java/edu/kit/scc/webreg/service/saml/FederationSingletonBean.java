package edu.kit.scc.webreg.service.saml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;

@Singleton
public class FederationSingletonBean {

	@Inject
	private Logger logger;
	
	@Inject
	private SamlIdpMetadataDao idpDao;
	
	@Inject
	private FederationDao federationDao;
	
	private Map<FederationEntity, List<SamlIdpMetadataEntity>> federationCache;
	private List<FederationEntity> sortedFederationList;
	private Map<String, SamlIdpMetadataEntity> idpMap;
	
	@PostConstruct
	public void init() {
		logger.info("Constructing federation cache");
		federationCache = new HashMap<FederationEntity, List<SamlIdpMetadataEntity>>();
		idpMap = new HashMap<String, SamlIdpMetadataEntity>();
		refreshCache();
	}
	
	public void refreshCache() {
		sortedFederationList = federationDao.findAll();

		idpMap.clear();
		
		for (FederationEntity federation : sortedFederationList) {
			logger.info("Loading federation {} ({})", federation.getId(), federation.getEntityId());
			
			List<SamlIdpMetadataEntity> idpList = idpDao.findAllByFederationOrderByOrgname(federation);
			federationCache.put(federation, idpList);
			for (SamlIdpMetadataEntity idp : idpList) {
				idpMap.put(idp.getEntityId(), idp);
			}
		}
	}
	
	public List<FederationEntity> getFederationList() {
		return sortedFederationList;
	}
	
	public List<SamlIdpMetadataEntity> getIdpList(FederationEntity federationEntity) {
		return federationCache.get(federationEntity);
	}
	
	public List<SamlIdpMetadataEntity> getAllIdpList() {
		List<SamlIdpMetadataEntity> tempList = new ArrayList<SamlIdpMetadataEntity>(idpMap.values());
		Collections.sort(tempList, new Comparator<SamlIdpMetadataEntity>() {

			@Override
			public int compare(SamlIdpMetadataEntity idp1, SamlIdpMetadataEntity idp2) {
				if (idp1 != null && idp1.getOrgName() != null &&
						idp2 != null && idp2.getOrgName() != null)
					return idp1.getOrgName().compareTo(idp2.getOrgName());
				else
					return 0;
			}
			
		});
		return tempList;
	}
}
