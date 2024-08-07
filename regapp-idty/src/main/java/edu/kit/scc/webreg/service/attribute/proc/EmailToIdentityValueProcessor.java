package edu.kit.scc.webreg.service.attribute.proc;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.identity.EmailAddressStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityEmailAddressHandler;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.mail.internet.AddressException;

public class EmailToIdentityValueProcessor extends StringListMergeValueProcessor {

	public EmailToIdentityValueProcessor(String outputAttribute, String... inspectValues) {
		super(outputAttribute, inspectValues);
	}

	public void apply(IdentityAttributeSetEntity attributeSet) {
		super.apply(attributeSet);

		Set<String> emailAddresses = new HashSet<>();
		for (ValueEntity value : getValueList()) {
			if (value instanceof StringValueEntity) {
				emailAddresses.add(((StringValueEntity) value).getValueString());
			} else if (value instanceof StringListValueEntity) {
				if (((StringListValueEntity) value).getValueList() != null) {
					emailAddresses.addAll(((StringListValueEntity) value).getValueList());
				}
			}
		}

		IdentityEntity identity = attributeSet.getIdentity();
		Map<String, IdentityEmailAddressEntity> emailMap = identity.getEmailAddresses().stream()
				.collect(Collectors.toMap(IdentityEmailAddressEntity::getEmailAddress, Function.identity()));
		
		for (String email : emailAddresses) {
			// Add email addresses from attribute sources to identity
			if (! emailMap.containsKey(email)) {
				createIdentityEmailAddress(identity, email);
			}
		}
		for (Entry<String, IdentityEmailAddressEntity> entry : emailMap.entrySet()) {
			// Remove email addresses which are attribute types and no longer there
			if (EmailAddressStatus.FROM_ATTRIBUTE_UNVERIFIED.equals(entry.getValue().getEmailStatus()) ||
					EmailAddressStatus.FROM_ATTRIBUTE_VERIFIED.equals(entry.getValue().getEmailStatus())) {
				if (! emailAddresses.contains(entry.getValue().getEmailAddress())) {
					deleteIdentityEmailAddress(identity, entry.getValue());
				}
			}
		}		
	}
	
	private void deleteIdentityEmailAddress(IdentityEntity identity, IdentityEmailAddressEntity email) {
		IdentityEmailAddressHandler handler = getIdentityEmailAddressHandler();
		if (identity.getPrimaryEmail().equals(email))
			identity.setPrimaryEmail(null);
		handler.deleteEmailAddress(email, "idty-" + identity.getId());
	}
	
	private void createIdentityEmailAddress(IdentityEntity identity, String email) {
		IdentityEmailAddressHandler handler = getIdentityEmailAddressHandler();
		try {
			handler.addEmailAddressFromAttribute(identity, email, "idty-" + identity.getId());
		} catch (AddressException e) {
			logger.info("Unparsable email address: {} error: {}", email, e.getMessage());
		}
	}
	
	private IdentityEmailAddressHandler getIdentityEmailAddressHandler() {
		return CDI.current().select(IdentityEmailAddressHandler.class).get();
	}
}