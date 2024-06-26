package edu.kit.scc.webreg.service.attribute.proc;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityAttributeResolver;
import jakarta.enterprise.inject.spi.CDI;

public class EmailMergeValueProcessor extends SingleStringMergeValueProcessor {

	public EmailMergeValueProcessor(String outputAttribute, String... inspectValues) {
		super(outputAttribute, inspectValues);
	}

	public void apply(IdentityAttributeSetEntity attributeSet) {
		super.apply(attributeSet);

		IdentityEntity identity = attributeSet.getIdentity();
		if (identity.getPrimaryEmail() == null) {
			String email = getIdentityAttributeResolver().resolveSingleStringValue(identity, outputAttribute);
			Map<String, IdentityEmailAddressEntity> emailMap = identity.getEmailAddresses().stream()
					.collect(Collectors.toMap(IdentityEmailAddressEntity::getEmailAddress, Function.identity()));
			if (emailMap.containsKey(email)) {
				identity.setPrimaryEmail(emailMap.get(email));
			}
		}
	}
	
	private IdentityAttributeResolver getIdentityAttributeResolver() {
		return CDI.current().select(IdentityAttributeResolver.class).get();
	}
}
