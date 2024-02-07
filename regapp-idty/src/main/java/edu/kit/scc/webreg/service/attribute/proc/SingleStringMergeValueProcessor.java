package edu.kit.scc.webreg.service.attribute.proc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public class SingleStringMergeValueProcessor extends AbstractListProcessor {
	
	private String outputAttribute;
	private String[] inspectValues;
	
	public SingleStringMergeValueProcessor(String outputAttribute, String... inspectValues) {
		this.outputAttribute = outputAttribute;
		this.inspectValues = inspectValues;
	}

	public void apply(IdentityAttributeSetEntity attributeSet) {
		LocalAttributeEntity attribute = getValueUpdater().resolveAttribute(outputAttribute);
		StringValueEntity targetValue = (StringValueEntity) getValueUpdater().resolveValue(attribute, attributeSet, StringValueEntity.class);
		
		Map<String, Integer> prioMap = new HashMap<>();
		Map<String, List<ValueEntity>> valueMap = new HashMap<>();
		for (ValueEntity value : getValueList()) {
			if (value instanceof StringValueEntity) {
				String name = ((StringValueEntity) value).getValueString();
				logger.debug("Raising prio for attribute {} and value {}", attribute.getName(), name);
				if (value.getAttributeSet().getPrio() != null)
					raisePrio(prioMap, name, 1 + value.getAttributeSet().getPrio());
				else
					raisePrio(prioMap, name, 1);
				putInValueMap(valueMap, name, value);
			}
			else {
				logger.warn("Attribute for {} is not single string. Only single string is supported.", attribute.getName());
			}
		}
		
		String name = Collections.max(prioMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
		logger.debug("Winner for attribute {} is value {} (prio {})", attribute.getName(), name, prioMap.get(name));
		targetValue.setValueString(name);
		valueMap.get(name).stream().forEach(v -> v.getNextValues().add(targetValue));
		targetValue.setEndValue(true);
	}

	private void putInValueMap(Map<String, List<ValueEntity>> valueMap, String key, ValueEntity value) {
		if (! valueMap.containsKey(key)) {
			valueMap.put(key, new ArrayList<>());
		}
		valueMap.get(key).add(value);
	}
	
	private void raisePrio(Map<String, Integer> prioMap, String key, Integer byPoints) {
		if (! prioMap.containsKey(key)) {
			prioMap.put(key, byPoints);
		}
		else {
			prioMap.put(key, prioMap.get(key) + byPoints);
		}
	}
	
	@Override
	protected List<String> getInspectValueNames() {
		return Arrays.asList(inspectValues);
	}
}
