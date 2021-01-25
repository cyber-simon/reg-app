package edu.kit.scc.webreg.service.saml;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;

@ApplicationScoped
public class HomeIdResolver {

	@Inject
	private AttributeMapHelper attrHelper;
	
	public String resolveHomeId(SamlUserEntity user, Map<String, List<Object>> attributeMap) {
		String homeId = null;
		
		if (user.getIdp().getGenericStore().containsKey("prefix")) {
			homeId = user.getIdp().getGenericStore().get("prefix");
		}
		else {
			homeId = attrHelper.getSingleStringFirst(attributeMap, "http://bwidm.de/bwidmOrgId");
		}

		return homeId;
	}
	
}
