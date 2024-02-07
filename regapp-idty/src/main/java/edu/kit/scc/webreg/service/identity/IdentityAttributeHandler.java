package edu.kit.scc.webreg.service.identity;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalIdentityAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.LocalUserAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.attribute.proc.IdentityValuesProcessor;
import edu.kit.scc.webreg.service.attribute.proc.ValueUpdater;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IdentityAttributeHandler {

	@Inject
	private Logger logger;

	@Inject
	private ValueDao valueDao;

	@Inject
	private ValueUpdater valueUpdater;

	@Inject
	private LocalUserAttributeSetDao localAttributeSetDao;

	@Inject
	private LocalIdentityAttributeSetDao identityAttributeSetDao;

	public void updateAttributes(IdentityEntity identity, UserEntity actualUser) {

		IdentityValuesProcessor processor = new IdentityValuesProcessor(valueUpdater);
		
		identity.getUsers().stream().forEach(user -> {
			final LocalUserAttributeSetEntity attributeSet = localAttributeSetDao
					.find(equal(LocalUserAttributeSetEntity_.user, user));
			final List<ValueEntity> valueList = valueDao
					.findAll(and(equal(ValueEntity_.endValue, true), equal(ValueEntity_.attributeSet, attributeSet)));
			for (ValueEntity value : valueList) {
				processor.addValue(value);
			}
		});

		final IdentityAttributeSetEntity attributeSet = resolveIdentityAttributeSet(identity);

		processor.apply(attributeSet);
		
	}

	private LocalIdentityAttributeSetEntity resolveIdentityAttributeSet(IdentityEntity identity) {
		LocalIdentityAttributeSetEntity attributeSet = identityAttributeSetDao.find(equal(LocalIdentityAttributeSetEntity_.identity, identity));
		if (attributeSet == null) {
			attributeSet = identityAttributeSetDao.createNew();
			attributeSet.setIdentity(identity);
			attributeSet = identityAttributeSetDao.persist(attributeSet);
		}
		return attributeSet;
	}
	
}
