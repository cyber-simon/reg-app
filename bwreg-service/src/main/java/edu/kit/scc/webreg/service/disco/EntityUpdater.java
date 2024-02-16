package edu.kit.scc.webreg.service.disco;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.dao.jpa.IconCacheDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.IconCacheEntity;
import edu.kit.scc.webreg.entity.ImageDataEntity;
import edu.kit.scc.webreg.entity.ImageType;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
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
	public SamlIdpMetadataEntity updateIdpEntity(EntityDescriptor ed, FederationEntity federation) {
		SamlIdpMetadataEntity idp = idpDao.findByEntityId(ed.getEntityID());
		federation = federationDao.fetch(federation.getId());

		Boolean newIdp = (idp == null ? true : false);
		if (newIdp) {
			idp = idpDao.createNew();
			idp = idpDao.persist(idp);
			idp.setFederations(new HashSet<FederationEntity>());
			logger.info("Creating new idp {}", ed.getEntityID());
			idp.setScopes(new HashSet<>());
		}

		idp.setEntityId(ed.getEntityID());
		idp.setEntityDescriptor(samlHelper.marshal(ed));
		idp.setOrgName(metadataHelper.getOrganisation(ed));
		idp.setLogoUrl(metadataHelper.getLogo(ed));
		idp.setLogoSmallUrl(metadataHelper.getLogoSmall(ed));
		idp.getFederations().add(federation);
		idp.setStatus(SamlMetadataEntityStatus.ACTIVE);

		metadataHelper.fillDisplayData(ed, idp);
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

		createMissingLogos(idp);

		if (idp.getLogoSmallUrl() != null) {
			addLogo(idp.getLogoSmallUrl(), idp.getIcon());
		}

		if (idp.getLogoUrl() != null) {
			addLogo(idp.getLogoUrl(), idp.getIconLarge());
		}

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

	private void createMissingLogos(SamlIdpMetadataEntity idp) {
		if (idp.getIcon() == null) {
			IconCacheEntity icon = iconDao.createNew();
			icon = iconDao.persist(icon);
			idp.setIcon(icon);
		}

		if (idp.getIconLarge() == null) {
			IconCacheEntity icon = iconDao.createNew();
			icon = iconDao.persist(icon);
			idp.setIconLarge(icon);
		}
	}
}
