package edu.kit.scc.webreg.service.attribute;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.IncomingSamlAttributeDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingSamlAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
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

	public IncomingAttributeSetEntity createOrUpdateAttributes(UserEntity user, Map<String, List<Object>> attributeMap) {
		IncomingAttributeSetEntity incomingAttributeSet = getIncomingAttributeSet(user);

		final IncomingAttributeSetEntity incomingSet = incomingAttributeSet;
		attributeMap.entrySet().stream()
				.forEach(entry -> createOrUpdateSamlAttribute(incomingSet, entry.getKey(), entry.getValue()));
		
		return incomingAttributeSet;
	}

	public void createOrUpdateSamlAttribute(IncomingAttributeSetEntity incomingAttributeSet, String name,
			List<Object> attributeList) {
		if (attributeList == null) {
			logger.info("No value for {}", name);
		} else if (attributeList.size() == 1) {
			// Single element
			if (attributeList.get(0) instanceof String) {
				IncomingSamlAttributeEntity attribute = getAttribute(name, ValueType.STRING);
				createOrUpdateStringValue(incomingAttributeSet, attribute, name, (String) attributeList.get(0));
			}
		} else if (attributeList.size() > 1) {
			if (attributeList.get(0) instanceof String) {
				IncomingSamlAttributeEntity attribute = getAttribute(name, ValueType.STRING_LIST);
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
	protected List<Function<ValueEntity, ValueEntity>> getProcessingFunctions(LocalUserAttributeSetEntity localAttributeSet) {
		return Arrays.asList(new SamlMapLocalAttributeFunction(valueUpdater, valueDao, localAttributeDao, localAttributeSet));
	}
}
