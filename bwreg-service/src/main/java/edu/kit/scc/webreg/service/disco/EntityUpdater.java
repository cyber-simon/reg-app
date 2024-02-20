package edu.kit.scc.webreg.service.disco;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.dao.jpa.IconCacheDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.IconCacheEntity;
import edu.kit.scc.webreg.entity.ImageDataEntity;
import edu.kit.scc.webreg.entity.ImageType;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@TransactionManagement(TransactionManagementType.BEAN)
public class EntityUpdater {

	@Inject
	private Logger logger;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private SamlAAMetadataDao aaDao;

	@Inject
	private SamlSpMetadataDao spDao;

	@Inject
	private FederationDao federationDao;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private SamlIdpScopeDao idpScopeDao;

	@Inject
	private IconCacheDao iconDao;

	@Inject
	private MetadataHelper metadataHelper;

	@RetryTransaction
	public SamlIdpMetadataEntity removeIdpFromFederation(SamlIdpMetadataEntity idp, FederationEntity federation) {
		idp = idpDao.fetch(idp.getId());
		idp.getFederations().remove(federation);

		if (idp.getFederations().size() == 0) {
			// IDP is orphaned, set Status to DELETED
			idp.setStatus(SamlMetadataEntityStatus.DELETED);
		} else {
			idp.setStatus(SamlMetadataEntityStatus.ACTIVE);
		}
		return idp;
	}

	@RetryTransaction
	public SamlAAMetadataEntity removeAaFromFederation(SamlAAMetadataEntity aa, FederationEntity federation) {
		aa = aaDao.fetch(aa.getId());
		aa.getFederations().remove(federation);

		if (aa.getFederations().size() == 0) {
			// IDP is orphaned, set Status to DELETED
			aa.setStatus(SamlMetadataEntityStatus.DELETED);
		} else {
			aa.setStatus(SamlMetadataEntityStatus.ACTIVE);
		}
		return aa;
	}

	@RetryTransaction
	public SamlSpMetadataEntity removeSpFromFederation(SamlSpMetadataEntity sp, FederationEntity federation) {
		sp = spDao.fetch(sp.getId());
		sp.getFederations().remove(federation);

		if (sp.getFederations().size() == 0) {
			// IDP is orphaned, set Status to DELETED
			sp.setStatus(SamlMetadataEntityStatus.DELETED);
		} else {
			sp.setStatus(SamlMetadataEntityStatus.ACTIVE);
		}
		return sp;
	}
		
	@RetryTransaction
	public SamlSpMetadataEntity updateSpEntity(EntityDescriptor ed, FederationEntity federation) {
		SamlSpMetadataEntity sp = spDao.findByEntityId(ed.getEntityID());
		federation = federationDao.fetch(federation.getId());

		if (sp == null) {
			sp = spDao.createNew();
			sp.setFederations(new HashSet<FederationEntity>());
			logger.info("Creating new aa {}", ed.getEntityID());
			sp = spDao.persist(sp);
		}

		setSpMetadata(sp, ed);
		sp.getFederations().add(federation);
		sp.setStatus(SamlMetadataEntityStatus.ACTIVE);

		return sp;
	}
	
	@RetryTransaction
	public SamlAAMetadataEntity updateAaEntity(EntityDescriptor ed, FederationEntity federation) {
		SamlAAMetadataEntity aa = aaDao.findByEntityId(ed.getEntityID());
		federation = federationDao.fetch(federation.getId());

		if (aa == null) {
			aa = aaDao.createNew();
			aa.setFederations(new HashSet<FederationEntity>());
			logger.info("Creating new aa {}", ed.getEntityID());
			aa = aaDao.persist(aa);
		}

		setMetadata(aa, ed);
		aa.getFederations().add(federation);
		aa.setStatus(SamlMetadataEntityStatus.ACTIVE);

		createAndAddMissingLogos(aa);

		return aa;
	}
	
	@RetryTransaction
	public SamlIdpMetadataEntity updateIdpEntity(EntityDescriptor ed, FederationEntity federation) {
		SamlIdpMetadataEntity idp = idpDao.findByEntityId(ed.getEntityID());
		federation = federationDao.fetch(federation.getId());

		if (idp == null) {
			idp = idpDao.createNew();
			idp = idpDao.persist(idp);
			idp.setFederations(new HashSet<FederationEntity>());
			logger.info("Creating new idp {}", ed.getEntityID());
			idp.setScopes(new HashSet<>());
		}

		setMetadata(idp, ed);

		idp.getFederations().add(federation);
		idp.setStatus(SamlMetadataEntityStatus.ACTIVE);

		idp.setEntityCategoryList(metadataHelper.getEntityCategoryList(ed));

		Set<SamlIdpScopeEntity> scopes = metadataHelper.getScopes(ed, idp);

		Set<SamlIdpScopeEntity> oldScopes = idp.getScopes();

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

		createAndAddMissingLogos(idp);

		return idp;
	}

	private void addLogo(String logoUrl, IconCacheEntity icon) {
		if (icon.getImageData() == null) {
			icon.setImageData(new ImageDataEntity());
		}
		if (logoUrl.startsWith("data:image/")) {
			// embedded logo
			icon.setUrl(null);
			byte[] data = null;
			ImageType type = null;
			if (logoUrl.startsWith("data:image/png;base64,")) {
				data = Base64.getDecoder().decode(logoUrl.substring("data:image/png;base64,".length()));
				type = ImageType.PNG;
			} else if (logoUrl.startsWith("data:image/jpeg;base64,")) {
				data = Base64.getDecoder().decode(logoUrl.substring("data:image/jpeg;base64,".length()));
				type = ImageType.JPEG;
			} else if (logoUrl.startsWith("data:image/svg+xml;base64,")) {
				data = Base64.getDecoder().decode(logoUrl.substring("data:image/svg+xml;base64,".length()));
				type = ImageType.SVG;
			} else {
				logger.debug("IDP Logo no handler for type. ({}): startswith {}", logoUrl.length(),
						logoUrl.substring(0, 64));
			}
			icon.setImageType(type);
			icon.getImageData().setData(data);
		} else {
			if (logoUrl.length() < 4012)
				icon.setUrl(logoUrl);
			else
				logger.debug("IDP Logo URL size too big ({}): startswith {}", logoUrl.length(), logoUrl.substring(0, 64));
		}
	}

	private void createAndAddMissingLogos(SamlMetadataEntity entity) {
		if (entity.getIcon() == null) {
			IconCacheEntity icon = iconDao.createNew();
			icon = iconDao.persist(icon);
			entity.setIcon(icon);
		}

		if (entity.getIconLarge() == null) {
			IconCacheEntity icon = iconDao.createNew();
			icon = iconDao.persist(icon);
			entity.setIconLarge(icon);
		}
		
		if (entity.getLogoSmallUrl() != null) {
			addLogo(entity.getLogoSmallUrl(), entity.getIcon());
		}

		if (entity.getLogoUrl() != null) {
			addLogo(entity.getLogoUrl(), entity.getIconLarge());
		}
	}
	
	private void setMetadata(SamlMetadataEntity entity, EntityDescriptor ed) {
		entity.setEntityId(ed.getEntityID());
		entity.setEntityDescriptor(samlHelper.marshal(ed));
		entity.setOrgName(metadataHelper.getOrganisation(ed));
		entity.setLogoUrl(metadataHelper.getLogo(ed));
		entity.setLogoSmallUrl(metadataHelper.getLogoSmall(ed));
		metadataHelper.fillDisplayData(ed, entity);
	}

	private void setSpMetadata(SamlSpMetadataEntity entity, EntityDescriptor ed) {
		entity.setEntityId(ed.getEntityID());
		entity.setEntityDescriptor(samlHelper.marshal(ed));
		entity.setOrgName(metadataHelper.getOrganisation(ed));
		entity.setLogoUrl(metadataHelper.getLogo(ed));
		entity.setLogoSmallUrl(metadataHelper.getLogoSmall(ed));
		metadataHelper.fillDisplayData(ed, entity);
	}
}
