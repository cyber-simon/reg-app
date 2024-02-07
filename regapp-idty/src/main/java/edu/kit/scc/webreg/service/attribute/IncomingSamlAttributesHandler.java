package edu.kit.scc.webreg.service.attribute;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.IncomingSamlAttributeDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingSamlAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.service.attribute.proc.SamlMapLocalAttributeFunction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IncomingSamlAttributesHandler extends IncomingAttributesHandler<IncomingSamlAttributeEntity> {

	@Inject
	private Logger logger;

	@Inject
	private IncomingSamlAttributeDao samlAttributeDao;

	public IncomingAttributeSetEntity createOrUpdateAttributes(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		final IncomingAttributeSetEntity incomingSet = getIncomingAttributeSet(user);
		Map<String, ValueEntity> actualValueMap = getValueList(incomingSet).stream()
				.collect(Collectors.toMap(v -> v.getAttribute().getName(), v -> v));
		attributeMap.entrySet().stream().forEach(entry -> {
			createOrUpdateSamlAttribute(incomingSet, entry.getKey(), entry.getValue());
			actualValueMap.remove(entry.getKey());
		});

		removeValues(actualValueMap);

		return incomingSet;
	}

	public void createOrUpdateSamlAttribute(IncomingAttributeSetEntity incomingAttributeSet, String name,
			List<Object> attributeList) {
		if (attributeList == null) {
			logger.info("No value for {}", name);
		} else if (singleValueSet().contains(name)) {
			if (attributeList.get(0) instanceof String) {
				IncomingSamlAttributeEntity attribute = getAttribute(name);
				createOrUpdateStringValue(incomingAttributeSet, attribute, name, (String) attributeList.get(0));
			}
		} else if (multiValueSet().contains(name)) {
			if (attributeList.get(0) instanceof String) {
				IncomingSamlAttributeEntity attribute = getAttribute(name);
				createOrUpdateStringListValue(incomingAttributeSet, attribute, name,
						attributeList.stream().map(e -> (String) e).toList());
			}
		} else if (attributeList.size() == 1) {
			// Fallback for single element
			if (attributeList.get(0) instanceof String) {
				IncomingSamlAttributeEntity attribute = getAttribute(name);
				createOrUpdateStringValue(incomingAttributeSet, attribute, name, (String) attributeList.get(0));
			}
		} else if (attributeList.size() > 1) {
			// Fallback for multi element
			if (attributeList.get(0) instanceof String) {
				IncomingSamlAttributeEntity attribute = getAttribute(name);
				createOrUpdateStringListValue(incomingAttributeSet, attribute, name,
						attributeList.stream().map(e -> (String) e).toList());
			}
		}
	}

	@Override
	protected IncomingSamlAttributeDao getDao() {
		return samlAttributeDao;
	}

	@Override
	protected List<Function<ValueEntity, ValueEntity>> getProcessingFunctions(
			LocalUserAttributeSetEntity localAttributeSet) {
		return Arrays.asList(
				new SamlMapLocalAttributeFunction(valueUpdater, valueDao, localAttributeDao, localAttributeSet));
	}
	
	private Set<String> multiValueSet() {
		return new HashSet<>(Arrays.asList("urn:oid:1.3.6.1.4.1.5923.1.1.1.11", "urn:oid:0.9.2342.19200300.100.1.3",
				"urn:oid:1.3.6.1.4.1.5923.1.1.1.7", "urn:oid:1.3.6.1.4.1.5923.1.1.1.9"));
	}

	private Set<String> singleValueSet() {
		return new HashSet<>(Arrays.asList("urn:oid:2.5.4.4",
				"urn:oid:2.5.4.42"));
	}
}
