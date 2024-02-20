package edu.kit.scc.webreg.service.disco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kie.api.runtime.KieSession;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FederationUpdateService implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private FederationDao dao;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private SamlSpMetadataDao spDao;

	@Inject
	private SamlAAMetadataDao aaDao;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;

	@Inject
	private MetadataHelper metadataHelper;

	@Inject	
	private EntityUpdater entityUpdater;
	
	public void updateFederation(FederationEntity entity) {
		logger.info("Starting updateFederation for federation {}", entity.getName());

		entity = dao.fetch(entity.getId());
		
		EntitiesDescriptor entities = metadataHelper.fetchMetadata(entity.getFederationMetadataUrl());
		if (entities == null) {
			logger.info("Empty entities list, nothing to do.");
			return;
		}

		List<EntityDescriptor> entityList = metadataHelper.convertEntitiesDescriptor(entities);
		logger.debug("Got entity List size {}", entityList.size());

		if ((entity.getEntityCategoryFilter() != null) && (!entity.getEntityCategoryFilter().equals(""))) {
			logger.debug("Filtering entity category: {}", entity.getEntityCategoryFilter());
			entityList = metadataHelper.filterEntityCategory(entityList, entity.getEntityCategoryFilter());
		}

		logger.debug("Got Entity List size {}", entityList.size());

		if (entity.getEntityFilterRulePackage() != null) {
			long a = System.currentTimeMillis();

			BusinessRulePackageEntity rulePackage = entity.getEntityFilterRulePackage();
			KieSession ksession = knowledgeSessionService.getStatefulSession(rulePackage.getPackageName(),
					rulePackage.getKnowledgeBaseName(), rulePackage.getKnowledgeBaseVersion());

			ksession.setGlobal("logger", logger);
			for (EntityDescriptor ed : entityList) {
				ksession.insert(ed);
			}

			ksession.fireAllRules();
			List<Object> objectList = new ArrayList<Object>(ksession.getObjects());

			List<EntityDescriptor> filterEntityList = new ArrayList<EntityDescriptor>();
			for (Object o : objectList) {
				ksession.delete(ksession.getFactHandle(o));
				if (o instanceof EntityDescriptor)
					filterEntityList.add((EntityDescriptor) o);
			}

			entityList.removeAll(filterEntityList);

			logger.debug("Applying group filter drools took {} ms", (System.currentTimeMillis() - a));
			a = System.currentTimeMillis();
			logger.debug("Got IDP entity List size {}", entityList.size());
		}

		List<EntityDescriptor> idpList = new ArrayList<EntityDescriptor>();
		List<EntityDescriptor> spList = new ArrayList<EntityDescriptor>();
		List<EntityDescriptor> aaList = new ArrayList<EntityDescriptor>();

		if (entity.getFetchIdps()) {
			logger.debug("Getting IDPs");
			idpList.addAll(metadataHelper.filterIdps(entityList));
		}

		if (entity.getFetchSps()) {
			logger.debug("Getting SPs");
			spList.addAll(metadataHelper.filterSps(entityList));
		}

		if (entity.getFetchAAs()) {
			logger.debug("Getting AAs");
			aaList.addAll(metadataHelper.filterAAs(entityList));
		}

		entity.setEntityId(entities.getName());

		updateIdpEntities(entity, idpList);
		updateSpEntities(entity, spList);
		updateAAEntities(entity, aaList);

		entity.setPolledAt(new Date());
		logger.debug("Updated SAML Entities for Federation {}", entity.getName());
	}

	private void updateAAEntities(FederationEntity entity, List<EntityDescriptor> entityList) {
	
		List<SamlAAMetadataEntity> oldList = aaDao.findAllByFederation(entity);
		List<SamlAAMetadataEntity> updatedList = new ArrayList<SamlAAMetadataEntity>();

		for (EntityDescriptor ed : entityList) {
			SamlAAMetadataEntity aa = entityUpdater.updateAaEntity(ed, entity);
			updatedList.add(aa);
		}

		oldList.removeAll(updatedList);

		for (SamlAAMetadataEntity aa : oldList) {
			logger.info("remove sp {} from federation {}", aa.getEntityId(), entity.getEntityId());
			entityUpdater.removeAaFromFederation(aa, entity);
		}
	}

	private void updateSpEntities(FederationEntity entity, List<EntityDescriptor> entityList) {

		List<SamlSpMetadataEntity> oldList = spDao.findAllByFederation(entity);
		List<SamlSpMetadataEntity> updatedList = new ArrayList<SamlSpMetadataEntity>();

		for (EntityDescriptor ed : entityList) {
			SamlSpMetadataEntity sp = entityUpdater.updateSpEntity(ed, entity);
			updatedList.add(sp);
		}

		oldList.removeAll(updatedList);

		for (SamlSpMetadataEntity sp : oldList) {
			logger.info("remove sp {} from federation {}", sp.getEntityId(), entity.getEntityId());
			entityUpdater.removeSpFromFederation(sp, entity);
		}
	}

	private void updateIdpEntities(FederationEntity entity, List<EntityDescriptor> entityList) {

		List<SamlIdpMetadataEntity> oldList = idpDao.findAllByFederation(entity);
		List<SamlIdpMetadataEntity> updatedList = new ArrayList<SamlIdpMetadataEntity>();

		for (EntityDescriptor ed : entityList) {
			SamlIdpMetadataEntity idp = entityUpdater.updateIdpEntity(ed, entity);
			updatedList.add(idp);
		}

		oldList.removeAll(updatedList);

		for (final SamlIdpMetadataEntity idp : oldList) {
			logger.info("remove idp {} from federation {}", idp.getEntityId(), entity.getEntityId());
			entityUpdater.removeIdpFromFederation(idp, entity);
		}
	}

	
}
