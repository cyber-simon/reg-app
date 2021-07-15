package edu.kit.scc.webreg.service.saml;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;

@ApplicationScoped
public class HomeIdResolver {

	@Inject
	private AttributeMapHelper attrHelper;
	
	public String resolveHomeId(UserEntity user, Map<String, List<Object>> attributeMap) {
		String homeId = null;
		
		if (user instanceof SamlUserEntity) {
			SamlUserEntity samlUser = (SamlUserEntity) user;
			if (samlUser.getIdp().getGenericStore().containsKey("prefix")) {
				homeId = samlUser.getIdp().getGenericStore().get("prefix");
			}
			else {
				homeId = attrHelper.getSingleStringFirst(attributeMap, "http://bwidm.de/bwidmOrgId");
			}
		}
		else if (user instanceof OidcUserEntity) {
			OidcUserEntity oidcUser = (OidcUserEntity) user;
			if (oidcUser.getIssuer().getGenericStore().containsKey("prefix")) {
				homeId = oidcUser.getIssuer().getGenericStore().get("prefix");
			}
			else {
				homeId = oidcUser.getIssuer().getName();
			}
		}

		return homeId;
	}
	
}
