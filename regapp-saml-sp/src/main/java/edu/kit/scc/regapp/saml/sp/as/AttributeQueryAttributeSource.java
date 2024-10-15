package edu.kit.scc.regapp.saml.sp.as;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;

import edu.kit.scc.webreg.as.AbstractAttributeSourceWorkflow;
import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import jakarta.enterprise.inject.spi.CDI;

public class AttributeQueryAttributeSource extends AbstractAttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	private SamlIdpMetadataDao idpDao;
	private SamlAAMetadataDao aaDao;
	private SamlSpConfigurationDao spDao;
	private AttributeQueryHelper aqHelper;
	private Saml2AssertionService saml2AssertionService;
	private AttributeMapHelper attrHelper;
	private ScriptDao scriptDao;
	private ScriptingEnv scriptingEnv;

	public AttributeQueryAttributeSource() {
		super();
		idpDao = CDI.current().select(SamlIdpMetadataDao.class).get();
		aaDao = CDI.current().select(SamlAAMetadataDao.class).get();
		spDao = CDI.current().select(SamlSpConfigurationDao.class).get();
		saml2AssertionService = CDI.current().select(Saml2AssertionService.class).get();
		attrHelper = CDI.current().select(AttributeMapHelper.class).get();
		aqHelper = CDI.current().select(AttributeQueryHelper.class).get();
		scriptDao = CDI.current().select(ScriptDao.class).get();
		scriptingEnv = CDI.current().select(ScriptingEnv.class).get();
	}

	@Override
	public Boolean pollUserAttributes(ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, GroupDao groupDao,
			AttributeSourceAuditor auditor) throws UserUpdateException {

		init(asUserAttr, asValueDao, groupDao, auditor);

		String aaEntityId, spEntityId, nameIdScript;

		try {
			aaEntityId = prop.readProp("aa_entity_id");
			spEntityId = prop.readProp("sp_entity_id");
			nameIdScript = prop.readProp("name_id_scipt");
		} catch (PropertyReaderException e) {
			throw new UserUpdateException(
					"AS is not configured correctly. A parameter is missing. Configure aa_entity_id, sp_entity_id and name_id_scipt");
		}

		UserEntity user = asUserAttr.getUser();

		SamlSpConfigurationEntity spEntity = spDao.findByEntityId(spEntityId);
		SamlMetadataEntity idpEntity = idpDao.findByEntityId(aaEntityId);
		if (idpEntity == null) {
			idpEntity = aaDao.findByEntityId(aaEntityId);
			
			if (idpEntity == null) 
				throw new UserUpdateException("AS is not configured correctly. IDP or AA not found: " + aaEntityId);
		}

		ScriptEntity script = scriptDao.findByName(nameIdScript);
		if (script == null)
			throw new UserUpdateException("AS is not configured correctly. Script not found: " + nameIdScript);

		Assertion assertion;
		try {
			ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(script.getScriptEngine());
			engine.eval(script.getScript());
			Invocable invocable = (Invocable) engine;

			String format = (String) invocable.invokeFunction("resolveFormat", scriptingEnv, user, user.getIdentity(), logger,
					spEntity, idpEntity);
			String nameId = (String) invocable.invokeFunction("resolveId", scriptingEnv, user, user.getIdentity(), logger, spEntity,
					idpEntity);

			Response samlResponse = aqHelper.query(format, nameId, idpEntity, spEntity);
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
				} else if (entry.getValue().size() > 1) {
					condensedMap.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
				}
			}
		}
		Boolean changed = false;
		changed |= createOrUpdateValues(condensedMap);

		return changed;
	}
}
