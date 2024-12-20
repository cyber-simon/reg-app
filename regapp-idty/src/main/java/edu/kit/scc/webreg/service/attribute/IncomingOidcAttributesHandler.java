package edu.kit.scc.webreg.service.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.util.JSONArrayUtils;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import edu.kit.scc.regapp.oidc.tools.OidcTokenHelper;
import edu.kit.scc.webreg.dao.jpa.attribute.IncomingOidcAttributeDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingOidcAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@ApplicationScoped
public class IncomingOidcAttributesHandler extends IncomingAttributesHandler<IncomingOidcAttributeEntity> {

	@Inject
	private Logger logger;

	@Inject
	private IncomingOidcAttributeDao oidcAttributeDao;

	@Inject
	private OidcTokenHelper oidcTokenHelper;

	public IncomingAttributeSetEntity createOrUpdateAttributes(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		IncomingAttributeSetEntity incomingAttributeSet = getIncomingAttributeSet(user);
		Map<String, ValueEntity> actualValueMap = getValueList(incomingAttributeSet).stream()
				.collect(Collectors.toMap(v -> v.getAttribute().getName(), v -> v));

		IDTokenClaimsSet idToken = oidcTokenHelper.claimsFromMap(attributeMap);
		if (idToken != null)
			createOrUpdateOidcStringClaims(incomingAttributeSet, idToken, "claims", actualValueMap);

		UserInfo userInfo = oidcTokenHelper.userInfoFromMap(attributeMap);
		if (userInfo != null)
			createOrUpdateOidcStringClaims(incomingAttributeSet, userInfo, "userInfo", actualValueMap);

		removeValues(actualValueMap);

		return incomingAttributeSet;
	}

	public void createOrUpdateOidcStringClaims(IncomingAttributeSetEntity incomingAttributeSet, ClaimsSet claimSet,
			String baseName, Map<String, ValueEntity> actualValueMap) {
		JSONObject jsonBase = claimSet.toJSONObject();
		jsonBase.entrySet().forEach(entry -> {
			if (entry.getKey() != null && entry.getValue() != null) {
				if (entry.getValue() instanceof String) {
					IncomingOidcAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateStringValue(incomingAttributeSet, attribute, entry.getKey(),
							(String) entry.getValue());
				} else if (entry.getValue() instanceof JSONArray) {
					IncomingOidcAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateStringListValue(incomingAttributeSet, attribute, entry.getKey(),
							JSONArrayUtils.toStringList((JSONArray) entry.getValue()));
				} else if (entry.getValue() instanceof Long) {
					IncomingOidcAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateLongValue(incomingAttributeSet, attribute, entry.getKey(), (Long) entry.getValue());
				} else {
					logger.warn("No handler for incoming attribute {}: Type {} not implemented", entry.getKey(),
							entry.getValue().getClass().getCanonicalName());
				}
			}
			actualValueMap.remove(entry.getKey());
		});
	}

	@Override
	protected IncomingOidcAttributeDao getDao() {
		return oidcAttributeDao;
	}

	@Override
	protected List<Function<ValueEntity, ValueEntity>> getProcessingFunctions(LocalUserAttributeSetEntity localAttributeSet) {
		return new ArrayList<>();
	}
}
