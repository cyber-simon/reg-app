package edu.kit.scc.webreg.service.attribute.proc;

import java.util.HashSet;
import java.util.Set;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.UserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;

public class PrioOnAttributeSetFunction extends AbstractSingularAttributePipe
		implements SingularAttributePipe<ValueEntity, ValueEntity> {

	public PrioOnAttributeSetFunction(ValueUpdater valueUpdater, ValueDao valueDao, LocalAttributeDao attributeDao,
			UserAttributeSetEntity attributeSet) {
		super(valueUpdater, valueDao, attributeDao, attributeSet);
	}

	@Override
	public ValueEntity apply(ValueEntity in) {
		if (in.getAttributeSet().getPrio() == null) {
			// Set Prio of User for the first time to 0 or configured value per IDP/OP
			UserEntity user = ((UserAttributeSetEntity) attributeSet).getUser();
			Integer prio = 0;
			
			if (user instanceof SamlUserEntity) {
				prio += parseIntWithNulls(((SamlUserEntity) user).getIdp().getGenericStore().get("attribute_prio"));
			}
			else if (user instanceof OidcUserEntity) {
				prio += parseIntWithNulls(((OidcUserEntity) user).getIssuer().getGenericStore().get("attribute_prio"));
			}
			in.getAttributeSet().setPrio(prio);
		}
		
		if (in.getAttribute().getName().equals("eduperson_assurance")) {
			// Raise prio according to assurance levels
			if (in instanceof StringListValueEntity) {
				Integer prio = in.getAttributeSet().getPrio();
				Set<String> assurances = new HashSet<>(((StringListValueEntity) in).getValueList());
				if (assurances.contains("https://refeds.org/assurance")) {
					if (assurances.contains("https://refeds.org/assurance/IAP/high"))
						prio += 29;
					else if (assurances.contains("https://refeds.org/assurance/IAP/medium"))
						prio += 17;
					else if (assurances.contains("https://refeds.org/assurance/IAP/low"))
						prio += 7;
					else if (assurances.contains("https://refeds.org/assurance/IAP/local-enterprise"))
						prio += 3;
				}
				in.getAttributeSet().setPrio(prio);
			}
		}
		
		
		return in;
	}
	
	private Integer parseIntWithNulls(String in) {
		return (in == null ? 0 : Integer.parseInt(in));
	}
}
