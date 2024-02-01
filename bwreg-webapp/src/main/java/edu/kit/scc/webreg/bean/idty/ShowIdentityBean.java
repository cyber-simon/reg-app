package edu.kit.scc.webreg.bean.idty;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.attributes.LocalIdentityAttributeSetService;
import edu.kit.scc.webreg.service.attributes.LocalUserAttributeSetService;
import edu.kit.scc.webreg.service.attributes.ValueService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ShowIdentityBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private UserService userService;

	@Inject
	private IdentityService identityService;

	@Inject
	private ValueService valueService;

	@Inject
	private LocalIdentityAttributeSetService localIdentityAttributeSetService;

	@Inject
	private LocalUserAttributeSetService localUserAttributeSetService;

	public void preRenderView(ComponentSystemEvent ev) {

	}

	public IdentityEntity getIdentity() {
		return identityService.fetch(sessionManager.getIdentityId());
	}

	public List<UserEntity> getUserList() {
		return userService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(UserEntity_.id)),
				equal(UserEntity_.identity, getIdentity()), UserEntity_.genericStore, UserEntity_.attributeStore,
				SamlUserEntity_.idp);
	}

	public List<ValueEntity> getValuesForAttributeSet(AttributeSetEntity set) {
		return valueService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(ValueEntity_.id)),
				and(equal(ValueEntity_.attributeSet, set), equal(ValueEntity_.endValue, true)),
				StringListValueEntity_.valueList);
	}

	public List<LocalIdentityAttributeSetEntity> getAttributeSetList() {
		return localIdentityAttributeSetService.findAllEagerly(unlimited(),
				Arrays.asList(ascendingBy(LocalIdentityAttributeSetEntity_.id)),
				equal(LocalIdentityAttributeSetEntity_.identity, getIdentity()));
	}

	public List<LocalUserAttributeSetEntity> getUserAttributeSetList(UserEntity user) {
		return localUserAttributeSetService.findAllEagerly(unlimited(),
				Arrays.asList(ascendingBy(LocalUserAttributeSetEntity_.id)),
				equal(LocalUserAttributeSetEntity_.user, user));
	}
}
