/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.FederationService;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.SamlHelper;

@Stateless
public class FederationServiceImpl extends BaseServiceImpl<FederationEntity, Long> implements FederationService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private FederationDao dao;
	
	@Inject
	private SamlIdpMetadataDao idpDao;
	
	@Inject
	private SamlIdpScopeDao idpScopeDao;

	@Inject
	private SamlSpMetadataDao spDao;
	
	@Inject
	private SamlAAMetadataDao aaDao;
	
	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject 
	private SamlHelper samlHelper;

	@Inject 
	private MetadataHelper metadataHelper;
	
	@Override
	public void updateFederation(FederationEntity entity) {
		logger.info("Starting updateFederation for federation {}", entity.getName());
		
		EntitiesDescriptor entities = metadataHelper.fetchMetadata(entity.getFederationMetadataUrl());
		List<EntityDescriptor> entityList = metadataHelper.convertEntitiesDescriptor(entities);
		logger.debug("Got entity List size {}", entityList.size());

		if ((entity.getEntityCategoryFilter() != null) && (! entity.getEntityCategoryFilter().equals(""))) {
			logger.debug("Filtering entity category: {}", entity.getEntityCategoryFilter());
			entityList = metadataHelper.filterEntityCategory(entityList, entity.getEntityCategoryFilter());
		}

		logger.debug("Got Entity List size {}", entityList.size());

		if (entity.getEntityFilterRulePackage() != null) {
			long a = System.currentTimeMillis();
			
			BusinessRulePackageEntity rulePackage = entity.getEntityFilterRulePackage();
			KieSession ksession = knowledgeSessionService.getStatefulSession(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
					rulePackage.getKnowledgeBaseVersion());
	
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
			
			logger.debug("Applying group filter drools took {} ms", (System.currentTimeMillis() - a)); a = System.currentTimeMillis();
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
		entity = dao.findById(entity.getId());

		updateIdpEntities(entity, idpList);
		updateSpEntities(entity, spList);
		updateAAEntities(entity, aaList);
		
		entity.setPolledAt(new Date());
		dao.persist(entity);
		logger.debug("Updated SAML Entities for Federation {}", entity.getName());
	}

	private void updateAAEntities(FederationEntity entity, List<EntityDescriptor> entityList) {
		
		List<SamlAAMetadataEntity> oldList = aaDao.findAllByFederation(entity);
		List<SamlAAMetadataEntity> updatedList = new ArrayList<SamlAAMetadataEntity>();

		for (EntityDescriptor ed : entityList) {
			SamlAAMetadataEntity aa = aaDao.findByEntityId(ed.getEntityID());

			Boolean newSp = (aa == null ? true : false);
			if (newSp) {
				aa = aaDao.createNew();
				aa.setFederations(new HashSet<FederationEntity>());
				logger.info("Creating new aa {}", ed.getEntityID());
			}

			aa.setEntityId(ed.getEntityID());
			aa.setEntityDescriptor(samlHelper.marshal(ed));
			aa.setOrgName(metadataHelper.getOrganisation(ed));
			aa.getFederations().add(entity);
			aa.setStatus(SamlMetadataEntityStatus.ACTIVE);
			
//			metadataHelper.fillDisplayData(ed, sp);
//			sp.setEntityCategoryList(metadataHelper.getEntityCategoryList(ed));
			
			aa = aaDao.persist(aa);

//			Set<SamlIdpScopeEntity> scopes = metadataHelper.getScopes(ed, idp);
//
//			List<SamlIdpScopeEntity> oldScopes;
//			if (newIdp) 
//				oldScopes = new ArrayList<SamlIdpScopeEntity>();
//			else
//				oldScopes = idpScopeService.findByIdp(idp);
//			
//			Set<SamlIdpScopeEntity> deleteScopes = new HashSet<SamlIdpScopeEntity>(oldScopes);
//			deleteScopes.removeAll(scopes);
//			for (SamlIdpScopeEntity scope : deleteScopes) {
//				logger.info("Deleting idp scope {}", scope.getScope());
//				idpScopeService.delete(scope);
//			}
//			
//			scopes.removeAll(oldScopes);
//			for (SamlIdpScopeEntity scope : scopes) {
//				logger.info("Creating new idp scope {}", scope.getScope());
//				idpScopeService.save(scope);
//			}
			
			updatedList.add(aa);
		}
		
		oldList.removeAll(updatedList);

		for (SamlAAMetadataEntity aa : oldList) {
			aa.getFederations().remove(entity);
			entity.getIdps().remove(aa);
			
			if (aa.getFederations().size() == 0) {
				//SP is orphaned, set Status to DELETED
				aa.setStatus(SamlMetadataEntityStatus.DELETED);
			}
			else {
				aa.setStatus(SamlMetadataEntityStatus.ACTIVE);
			}
			
			aa = aaDao.persist(aa);
			logger.info("remove sp {} from federation {}", aa.getEntityId(), entity.getEntityId());
		}				
	}
	
	
	private void updateSpEntities(FederationEntity entity, List<EntityDescriptor> entityList) {
		
		List<SamlSpMetadataEntity> oldList = spDao.findAllByFederation(entity);
		List<SamlSpMetadataEntity> updatedList = new ArrayList<SamlSpMetadataEntity>();

		for (EntityDescriptor ed : entityList) {
			SamlSpMetadataEntity sp = spDao.findByEntityId(ed.getEntityID());

			Boolean newSp = (sp == null ? true : false);
			if (newSp) {
				sp = spDao.createNew();
				sp.setFederations(new HashSet<FederationEntity>());
				logger.info("Creating new sp {}", ed.getEntityID());
			}

			sp.setEntityId(ed.getEntityID());
			sp.setEntityDescriptor(samlHelper.marshal(ed));
			sp.setOrgName(metadataHelper.getOrganisation(ed));
			sp.getFederations().add(entity);
			sp.setStatus(SamlMetadataEntityStatus.ACTIVE);
			
//			metadataHelper.fillDisplayData(ed, sp);
//			sp.setEntityCategoryList(metadataHelper.getEntityCategoryList(ed));
			
			sp = spDao.persist(sp);

//			Set<SamlIdpScopeEntity> scopes = metadataHelper.getScopes(ed, idp);
//
//			List<SamlIdpScopeEntity> oldScopes;
//			if (newIdp) 
//				oldScopes = new ArrayList<SamlIdpScopeEntity>();
//			else
//				oldScopes = idpScopeService.findByIdp(idp);
//			
//			Set<SamlIdpScopeEntity> deleteScopes = new HashSet<SamlIdpScopeEntity>(oldScopes);
//			deleteScopes.removeAll(scopes);
//			for (SamlIdpScopeEntity scope : deleteScopes) {
//				logger.info("Deleting idp scope {}", scope.getScope());
//				idpScopeService.delete(scope);
//			}
//			
//			scopes.removeAll(oldScopes);
//			for (SamlIdpScopeEntity scope : scopes) {
//				logger.info("Creating new idp scope {}", scope.getScope());
//				idpScopeService.save(scope);
//			}
			
			updatedList.add(sp);
		}
		
		oldList.removeAll(updatedList);

		for (SamlSpMetadataEntity sp : oldList) {
			sp.getFederations().remove(entity);
			entity.getIdps().remove(sp);
			
			if (sp.getFederations().size() == 0) {
				//SP is orphaned, set Status to DELETED
				sp.setStatus(SamlMetadataEntityStatus.DELETED);
			}
			else {
				sp.setStatus(SamlMetadataEntityStatus.ACTIVE);
			}
			
			sp = spDao.persist(sp);
			logger.info("remove sp {} from federation {}", sp.getEntityId(), entity.getEntityId());
		}				
	}
	
	private void updateIdpEntities(FederationEntity entity, List<EntityDescriptor> entityList) {
		
		List<SamlIdpMetadataEntity> oldList = idpDao.findAllByFederation(entity);
		List<SamlIdpMetadataEntity> updatedList = new ArrayList<SamlIdpMetadataEntity>();
		
		for (EntityDescriptor ed : entityList) {
			SamlIdpMetadataEntity idp = idpDao.findByEntityId(ed.getEntityID());

			Boolean newIdp = (idp == null ? true : false);
			if (newIdp) {
				idp = idpDao.createNew();
				idp.setFederations(new HashSet<FederationEntity>());
				logger.info("Creating new idp {}", ed.getEntityID());
			}

			idp.setEntityId(ed.getEntityID());
			idp.setEntityDescriptor(samlHelper.marshal(ed));
			idp.setOrgName(metadataHelper.getOrganisation(ed));
			idp.getFederations().add(entity);
			idp.setStatus(SamlMetadataEntityStatus.ACTIVE);
			
			metadataHelper.fillDisplayData(ed, idp);
			idp.setEntityCategoryList(metadataHelper.getEntityCategoryList(ed));
			
			idp = idpDao.persist(idp);

			Set<SamlIdpScopeEntity> scopes = metadataHelper.getScopes(ed, idp);

			List<SamlIdpScopeEntity> oldScopes;
			if (newIdp) 
				oldScopes = new ArrayList<SamlIdpScopeEntity>();
			else
				oldScopes = idpScopeDao.findByIdp(idp);
			
			Set<SamlIdpScopeEntity> deleteScopes = new HashSet<SamlIdpScopeEntity>(oldScopes);
			deleteScopes.removeAll(scopes);
			for (SamlIdpScopeEntity scope : deleteScopes) {
				logger.info("Deleting idp scope {}", scope.getScope());
				idpScopeDao.delete(scope);
			}
			
			scopes.removeAll(oldScopes);
			for (SamlIdpScopeEntity scope : scopes) {
				logger.info("Creating new idp scope {}", scope.getScope());
				idpScopeDao.persist(scope);
			}
			
			updatedList.add(idp);
		}
		
		oldList.removeAll(updatedList);

		for (SamlIdpMetadataEntity idp : oldList) {
			idp.getFederations().remove(entity);
			entity.getIdps().remove(idp);
			
			if (idp.getFederations().size() == 0) {
				//IDP is orphaned, set Status to DELETED
				idp.setStatus(SamlMetadataEntityStatus.DELETED);
			}
			else {
				idp.setStatus(SamlMetadataEntityStatus.ACTIVE);
			}
			
			idpDao.persist(idp);
			logger.info("remove idp {} from federation {}", idp.getEntityId(), entity.getEntityId());
		}		
	}

	@Override
	public List<FederationEntity> findAllWithIdpEntities() {
		return dao.findAllWithIdpEntities();
	}

	@Override
	public FederationEntity findWithIdpEntities(Long id) {
		return dao.findWithIdpEntities(id);
	}

	@Override
	protected BaseDao<FederationEntity, Long> getDao() {
		return dao;
	}
}
