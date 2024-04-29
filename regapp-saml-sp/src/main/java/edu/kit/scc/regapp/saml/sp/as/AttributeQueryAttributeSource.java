package edu.kit.scc.regapp.saml.sp.as;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

import edu.kit.scc.webreg.as.AbstractAttributeSourceWorkflow;
import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import jakarta.enterprise.inject.spi.CDI;

public class AttributeQueryAttributeSource extends AbstractAttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	private SamlIdpMetadataDao idpDao;
	private SamlSpConfigurationDao spDao;
	private AttributeQueryHelper aqHelper;
	private Saml2AssertionService saml2AssertionService;
	private AttributeMapHelper attrHelper;

	public AttributeQueryAttributeSource() {
		super();
		idpDao = CDI.current().select(SamlIdpMetadataDao.class).get();
		spDao = CDI.current().select(SamlSpConfigurationDao.class).get();
		saml2AssertionService = CDI.current().select(Saml2AssertionService.class).get();
		attrHelper = CDI.current().select(AttributeMapHelper.class).get();
		aqHelper = CDI.current().select(AttributeQueryHelper.class).get();
	}
	
	@Override
	public Boolean pollUserAttributes(ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, GroupDao groupDao,
			AttributeSourceAuditor auditor) throws UserUpdateException {

		init(asUserAttr, asValueDao, groupDao, auditor);

		String aaEntityId, spEntityId;
		
		try {
			aaEntityId = prop.readProp("aa_entity_id");
			spEntityId = prop.readProp("sp_entity_id");
		} catch (PropertyReaderException e) {
			throw new UserUpdateException("AS is not configured correctly", e);
		}

		SamlUserEntity user = (SamlUserEntity) asUserAttr.getUser();

		SamlSpConfigurationEntity spEntity = spDao.findByEntityId(spEntityId);
		SamlIdpMetadataEntity idpEntity = idpDao.findByEntityId(aaEntityId);

		Assertion assertion;
		try {
			Response samlResponse = aqHelper.query(user.getPersistentId(), idpEntity, spEntity);
			assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, null, spEntity, false);
		} catch (Exception e) {
			throw new UserUpdateException("User Update from AS failed", e);
		}

		Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);
		Map<String, Object> condensedMap = new HashMap<>();
		for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
			if (entry.getValue() != null) {
				if (entry.getValue().size() == 1) {
					condensedMap.put(entry.getKey(), attrHelper.getSingleStringFirst(entry.getValue()));
				}
				else if (entry.getValue().size() > 1) {
					condensedMap.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
				}
			}
		}
		Boolean changed = false;
		changed |= createOrUpdateValues(condensedMap);

		return changed;
	}

}
