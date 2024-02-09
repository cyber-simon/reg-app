package edu.kit.scc.webreg.service.attribute.proc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class SamlMapLocalAttributeFunction extends AbstractSingularAttributePipe
		implements SingularAttributePipe<ValueEntity, ValueEntity> {

	private Map<String, String> nameMap;

	public SamlMapLocalAttributeFunction(ValueUpdater valueUpdater, ValueDao valueDao, LocalAttributeDao attributeDao,
			AttributeSetEntity attributeSet) {
		super(valueUpdater, valueDao, attributeDao, attributeSet);
		nameMap = new HashMap<>();
		nameMap.put("urn:oid:2.5.4.4", "family_name");
		nameMap.put("urn:oid:2.5.4.42", "given_name");
		nameMap.put("urn:oid:2.5.4.11", "ou");
		nameMap.put("http://bwidm.de/bwidmOrgId", "bwidm_orgid");
		nameMap.put("urn:oid:1.3.6.1.4.1.5923.1.1.1.16", "orcid");
		nameMap.put("urn:oid:1.3.6.1.4.1.5923.1.1.1.11", "eduperson_assurance");
		nameMap.put("urn:oid:1.3.6.1.4.1.5923.1.1.1.13", "eduperson_targetedid");
		nameMap.put("urn:oid:1.3.6.1.4.1.57378.1.2", "bwcard_id");
		nameMap.put("urn:oid:1.3.6.1.4.1.57378.1.1", "bwcard_number");
		nameMap.put("urn:oid:1.3.6.1.4.1.57378.1.4", "bwcard_validuntil");
		nameMap.put("urn:oid:0.9.2342.19200300.100.1.3", "email");
		nameMap.put("urn:oid:0.9.2342.19200300.100.1.1", "uid");
		nameMap.put("urn:oid:1.3.6.1.4.1.25178.1.2.9", "schac_home_org");
		nameMap.put("urn:oid:1.3.6.1.4.1.5923.1.1.1.6", "eduperson_principal_name");
		nameMap.put("urn:oid:1.3.6.1.4.1.5923.1.1.1.7", "eduperson_entitlement");
		nameMap.put("urn:oid:1.3.6.1.4.1.5923.1.1.1.9", "eduperson_affiliation");
	}

	@Override
	public ValueEntity apply(ValueEntity in) {

		if (!nameMap.containsKey(in.getAttribute().getName())) {
			return in;
		}

		final String outName = nameMap.get(in.getAttribute().getName());

		logger.debug("Map value of local attribute {} to {}", in.getAttribute().getName(), outName);

		final LocalAttributeEntity attributeEntity = findLocalAttributeEntity(in, outName);

		ValueEntity out = in.getNextValues().stream().filter(ve -> ve.getAttribute().getName().equals(outName))
				.findFirst().orElseGet(() -> persistNewValueEntity(attributeEntity, in));

		valueUpdater.copyValue(in, out);

		in.setEndValue(false);
		return out;
	}
}
