package edu.kit.scc.webreg.service.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.util.JSONArrayUtils;

import edu.kit.scc.webreg.dao.jpa.attribute.IncomingOAuthAttributeDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingOAuthAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.minidev.json.JSONArray;

@ApplicationScoped
public class IncomingOAuthAttributesHandler extends IncomingAttributesHandler<IncomingOAuthAttributeEntity> {

	@Inject
	private Logger logger;

	@Inject
	private IncomingOAuthAttributeDao oauthAttributeDao;

	public IncomingAttributeSetEntity createOrUpdateAttributes(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		IncomingAttributeSetEntity incomingAttributeSet = getIncomingAttributeSet(user);
		Map<String, ValueEntity> actualValueMap = getValueList(incomingAttributeSet).stream()
				.collect(Collectors.toMap(v -> v.getAttribute().getName(), v -> v));

		@SuppressWarnings("unchecked")
		HashMap<String, Object> userMap = (HashMap<String, Object>) attributeMap.get("user").get(0);
		createOrUpdateOAuthUserMap(incomingAttributeSet, userMap, "user", actualValueMap);
		
		removeValues(actualValueMap);

		return incomingAttributeSet;
	}

	public void createOrUpdateOAuthUserMap(IncomingAttributeSetEntity incomingAttributeSet, HashMap<String, Object> userMap,
			String baseName, Map<String, ValueEntity> actualValueMap) {
		userMap.entrySet().stream().forEach(entry -> {
			if (entry.getKey() != null && entry.getValue() != null) {
				if (entry.getValue() instanceof String) {
					IncomingOAuthAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateStringValue(incomingAttributeSet, attribute, entry.getKey(),
							(String) entry.getValue());
				} else if (entry.getValue() instanceof JSONArray) {
					IncomingOAuthAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateStringListValue(incomingAttributeSet, attribute, entry.getKey(),
							JSONArrayUtils.toStringList((JSONArray) entry.getValue()));
				} else if (entry.getValue() instanceof Long) {
					IncomingOAuthAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateLongValue(incomingAttributeSet, attribute, entry.getKey(), (Long) entry.getValue());
				} else if (entry.getValue() instanceof Integer) {
					IncomingOAuthAttributeEntity attribute = getAttribute(entry.getKey());
					createOrUpdateLongValue(incomingAttributeSet, attribute, entry.getKey(), ((Integer) entry.getValue()).longValue());
				} else {
					logger.warn("No handler for incoming attribute {}: Type {} not implemented", entry.getKey(),
							entry.getValue().getClass().getCanonicalName());
				}
			}
			actualValueMap.remove(entry.getKey());
		});
	}

	@Override
	protected IncomingOAuthAttributeDao getDao() {
		return oauthAttributeDao;
	}

	@Override
	protected List<Function<ValueEntity, ValueEntity>> getProcessingFunctions(LocalUserAttributeSetEntity localAttributeSet) {
		return new ArrayList<>();
	}
}
