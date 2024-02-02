package edu.kit.scc.webreg.service.attribute.proc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.UserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.LongValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ValueUpdater {

	@Inject
	private Logger logger;

	@Inject
	private ValueDao valueDao;

	@Inject
	private LocalAttributeDao localAttributeDao;

	public void writeString(StringListValueEntity targetValue, StringValueEntity value) {
		logger.debug("Writing value for {} as string", targetValue.getAttribute().getName());
		
	}
	
	public void writeAsList(StringListValueEntity targetValue, List<ValueEntity> valueList) {
		logger.debug("Writing values for {} as list", targetValue.getAttribute().getName());
		Set<String> values = new HashSet<>();
		targetValue.getPrevValues().clear();
		for (ValueEntity value : valueList) {
			if (value instanceof StringValueEntity)
				values.add(((StringValueEntity) value).getValueString());
			else if (value instanceof StringListValueEntity) {
				if (((StringListValueEntity) value).getValueList() != null)
					values.addAll(((StringListValueEntity) value).getValueList());
			}
			value.getNextValues().add(targetValue);
		}
		if (targetValue.getValueList() == null)
			targetValue.setValueList(new ArrayList<>());
		targetValue.getValueList().clear();
		targetValue.getValueList().addAll(values);
		targetValue.setEndValue(true);
	}

	public void writeAsAuthorityList(StringListValueEntity targetValue, List<ValueEntity> valueList) {
		logger.debug("Writing values for {} as authority list", targetValue.getAttribute().getName());
		Set<String> values = new HashSet<>();
		targetValue.getPrevValues().clear();
		for (ValueEntity value : valueList) {
			if (value instanceof StringValueEntity) {
				values.add(((StringValueEntity) value).getValueString());
			} else if (value instanceof StringListValueEntity) {
				// step one value back, and see if it is a value from a user
				if (value.getPrevValues().size() == 1) {
					ValueEntity prev = value.getPrevValues().iterator().next();
					if (prev.getAttributeSet() instanceof UserAttributeSetEntity) {
						UserEntity user = ((UserAttributeSetEntity) prev.getAttributeSet()).getUser();
						if (user instanceof SamlUserEntity) {
							String entityId = ((SamlUserEntity) user).getIdp().getEntityId();
							try {
								URL url = new URL(entityId);
								String host = url.getHost();
								((StringListValueEntity) value).getValueList().stream().forEach(v -> {
									if (v.contains("#"))
										values.add(v);
									else
										values.add(v + "#" + host);
								});
							} catch (MalformedURLException e) {
								logger.info("Can't parse entityId {}, skipping attribute", entityId);
							}

						} else if (user instanceof OidcUserEntity) {
							OidcRpConfigurationEntity issuer = ((OidcUserEntity) user).getIssuer();
							if (issuer.getGenericStore().containsKey("authority_scope")) {
								final String authority = issuer.getGenericStore().get("authority_scope");
								((StringListValueEntity) value).getValueList().stream().forEach(v -> {
									if (v.contains("#"))
										values.add(v);
									else
										values.add(v + "#" + authority);
								});
							} else {
								((StringListValueEntity) value).getValueList().stream().forEach(v -> {
									if (v.contains("#"))
										values.add(v);
								});
							}
						}
					}
				}
			}
			value.getNextValues().add(targetValue);
		}
		targetValue.setValueList(new ArrayList<>(values));
		targetValue.setEndValue(true);
	}

	public ValueEntity resolveValue(LocalAttributeEntity attribute, IdentityAttributeSetEntity attributeSet,
			ValueType valueType) {
		ValueEntity value = valueDao
				.find(and(equal(ValueEntity_.attribute, attribute), equal(ValueEntity_.attributeSet, attributeSet)));
		if (value == null) {
			value = valueDao.createNew(valueType);
			value.setAttribute(attribute);
			value.setAttributeSet(attributeSet);
			value = valueDao.persist(value);
		}
		return value;
	}

	public LocalAttributeEntity resolveAttribute(String name, ValueType valueType) {
		LocalAttributeEntity attribute = localAttributeDao
				.find(and(equal(LocalAttributeEntity_.name, name), equal(LocalAttributeEntity_.valueType, valueType)));
		if (attribute == null) {
			attribute = localAttributeDao.createNew();
			attribute.setName(name);
			attribute.setValueType(valueType);
			attribute = localAttributeDao.persist(attribute);
		}
		return attribute;
	}

	public void copyValue(ValueEntity in, ValueEntity out) {
		// TODO implement all types
		if (in.getAttribute().getValueType().equals(ValueType.STRING))
			((StringValueEntity) out).setValueString(((StringValueEntity) in).getValueString());
		else if (in.getAttribute().getValueType().equals(ValueType.STRING_LIST))
			((StringListValueEntity) out)
					.setValueList(new ArrayList<String>(((StringListValueEntity) in).getValueList()));
		else if (in.getAttribute().getValueType().equals(ValueType.LONG))
			((LongValueEntity) out).setValueLong(((LongValueEntity) in).getValueLong());

	}
}
