package edu.kit.scc.webreg.service.identity;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalAttributeDao;
import edu.kit.scc.webreg.dao.jpa.attribute.LocalIdentityAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity_;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IdentityAttributeResolver {

	@Inject
	private ValueDao valueDao;

	@Inject
	private LocalAttributeDao localAttributeDao;

	@Inject
	private LocalIdentityAttributeSetDao identityAttributeSetDao;

	public LocalIdentityAttributeSetEntity getAttributeSet(IdentityEntity identity) {
		return identityAttributeSetDao.find(equal(LocalIdentityAttributeSetEntity_.identity, identity));
	}

	public LocalAttributeEntity getAttribute(String name) {
		return localAttributeDao.find(equal(LocalAttributeEntity_.name, name));
	}

	public String resolveSingleStringValue(IdentityEntity identity, String name) {
		ValueEntity value = valueDao.find(and(equal(ValueEntity_.attributeSet, getAttributeSet(identity)),
				equal(ValueEntity_.attribute, getAttribute(name))));
		return (value instanceof StringValueEntity ? ((StringValueEntity) value).getValueString() : null);
	}

	public List<String> resolveStringListValue(IdentityEntity identity, String name) {
		ValueEntity value = valueDao.find(and(equal(ValueEntity_.attributeSet, getAttributeSet(identity)),
				equal(ValueEntity_.attribute, getAttribute(name))));
		return (value instanceof StringListValueEntity ? ((StringListValueEntity) value).getValueList() : null);
	}
}
