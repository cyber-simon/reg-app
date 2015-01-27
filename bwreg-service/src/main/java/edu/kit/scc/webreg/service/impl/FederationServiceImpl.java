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
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.service.FederationService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlIdpScopeService;
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
	private SamlIdpMetadataService idpService;
	
	@Inject
	private SamlIdpScopeService idpScopeService;
	
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
		entityList = metadataHelper.filterSP(entityList);
		if ((entity.getEntityCategoryFilter() != null) && (! entity.getEntityCategoryFilter().equals("")))
			entityList = metadataHelper.filterEntityCategory(entityList, entity.getEntityCategoryFilter());
		logger.debug("Got IDP entity List size {}", entityList.size());
		
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
		
		entity.setEntityId(entities.getName());
		updateEntities(entity, entityList);
		logger.debug("Updated SAML Entities for Federation {}", entity.getName());
	}

	private void updateEntities(FederationEntity entity, List<EntityDescriptor> entityList) {
		
		entity = dao.findById(entity.getId());
		
		List<SamlIdpMetadataEntity> oldList = idpService.findAllByFederation(entity);
		List<SamlIdpMetadataEntity> updatedList = new ArrayList<SamlIdpMetadataEntity>();
		
		for (EntityDescriptor ed : entityList) {
			SamlIdpMetadataEntity idp = idpService.findByEntityId(ed.getEntityID());

			Boolean newIdp = (idp == null ? true : false);
			if (newIdp) {
				idp = idpService.createNew();
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
			
			idp = idpService.save(idp);

			Set<SamlIdpScopeEntity> scopes = metadataHelper.getScopes(ed, idp);

			List<SamlIdpScopeEntity> oldScopes;
			if (newIdp) 
				oldScopes = new ArrayList<SamlIdpScopeEntity>();
			else
				oldScopes = idpScopeService.findByIdp(idp);
			
			Set<SamlIdpScopeEntity> deleteScopes = new HashSet<SamlIdpScopeEntity>(oldScopes);
			deleteScopes.removeAll(scopes);
			for (SamlIdpScopeEntity scope : deleteScopes) {
				logger.info("Deleting idp scope {}", scope.getScope());
				idpScopeService.delete(scope);
			}
			
			scopes.removeAll(oldScopes);
			for (SamlIdpScopeEntity scope : scopes) {
				logger.info("Creating new idp scope {}", scope.getScope());
				idpScopeService.save(scope);
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
			
			idpService.save(idp);
			logger.info("remove idp {} from federation {}", idp.getEntityId(), entity.getEntityId());
		}		

		entity.setPolledAt(new Date());
		
		dao.persist(entity);
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
