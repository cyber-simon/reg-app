package edu.kit.scc.webreg.service.attribute.proc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.lang.reflect.InvocationTargetException;
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
			Class<? extends ValueEntity> desiredClass) {
		ValueEntity value = valueDao
				.find(and(equal(ValueEntity_.attribute, attribute), equal(ValueEntity_.attributeSet, attributeSet)));

		if (value != null && !(desiredClass.isInstance(value))) {
			logger.info("Value type has change for attribute {}: {} -> {}", attribute.getName(),
					value.getClass().getSimpleName(), desiredClass.getSimpleName());
			for (ValueEntity v : value.getPrevValues()) {
				v.getNextValues().remove(value);
			}
			valueDao.delete(value);
			value = null;
		}

		if (value == null) {
			try {
				value = desiredClass.getConstructor().newInstance();
				value.setAttribute(attribute);
				value.setAttributeSet(attributeSet);
				value = valueDao.persist(value);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException
					| InvocationTargetException e) {
				logger.error("Cannot create instance of class {}: {}", desiredClass.getName(), e.getMessage());
			}
		}
		return value;
	}

	public LocalAttributeEntity resolveAttribute(String name) {
		LocalAttributeEntity attribute = localAttributeDao.find(equal(LocalAttributeEntity_.name, name));
		if (attribute == null) {
			attribute = localAttributeDao.createNew();
			attribute.setName(name);
			attribute = localAttributeDao.persist(attribute);
		}
		return attribute;
	}

	public void copyValue(ValueEntity in, ValueEntity out) {
		// TODO implement all types
		if (in instanceof StringValueEntity)
			copyStringValue((StringValueEntity) in, (StringValueEntity) out);
		else if (in instanceof StringListValueEntity)
			copyStringListValue((StringListValueEntity) in, (StringListValueEntity) out);
		else if (in instanceof LongValueEntity)
			((LongValueEntity) out).setValueLong(((LongValueEntity) in).getValueLong());

	}

	private void copyStringValue(StringValueEntity in, StringValueEntity out) {
		if (in.getValueString() == null)
			throw new IllegalArgumentException("String value can not be null");
		else if (in.getValueString() != null && (!in.getValueString().equals(out.getValueString()))) {
			logger.debug("Value {} and {} change from {} -> {}", in.getId(), out.getId(), out.getValueString(),
					in.getValueString());
			out.setValueString(in.getValueString());
		} else {
			logger.debug("Value {} and {} are unchanged", in.getId(), out.getId());
		}
	}

	private void copyStringListValue(StringListValueEntity in, StringListValueEntity out) {
		if (out.getValueList() == null) {
			out.setValueList(new ArrayList<>());
		}
		List<String> valuesToRemove = new ArrayList<>(out.getValueList());
		valuesToRemove.removeAll(in.getValueList());

		List<String> valuesToAdd = new ArrayList<>(in.getValueList());
		valuesToAdd.removeAll(out.getValueList());

		logger.debug("Value {} and {} remove {} values", in.getId(), out.getId(), valuesToRemove.size());
		logger.debug("Value {} and {} add {} values", in.getId(), out.getId(), valuesToAdd.size());

		out.getValueList().removeAll(valuesToRemove);
		out.getValueList().addAll(valuesToAdd);
	}
}
