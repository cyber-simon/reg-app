package edu.kit.scc.webreg.bean.ar;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.attributes.AttributeReleaseService;
import edu.kit.scc.webreg.service.attributes.ValueService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ShowAttributeReleaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private IdentityService identityService;

	@Inject
	private ValueService valueService;

	@Inject
	private AttributeReleaseService attributeReleaseService;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private IdentityEntity identity;
	private Long id;
	private AttributeReleaseEntity release;
	private List<ValueEntity> valueList;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			if (session.getIdentityId() != null) {
				identity = identityService.fetch(session.getIdentityId());
			}

			if (identity == null) {
				throw new IllegalStateException("User ID missing.");
			}
		}
	}
	
	public void revoke() {
		messageGenerator.addResolvedInfoMessage("attribute_release.revoke_message", "attribute_release.revoke_message", true);
		attributeReleaseService.revoke(release);
		release = null;
		valueList = null;
	}
	
	public void accept() {
		
	}
	
	public IdentityEntity getIdentity() {
		return identity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ValueEntity> getValueList() {
		if (valueList == null) {
			valueList = valueService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(ValueEntity_.id)),
					equal(ValueEntity_.attributeRelease, getRelease()),
					StringListValueEntity_.valueList);
		}
		return valueList;
	}
	
	public AttributeReleaseEntity getRelease() {
		if (release == null) {
			release = attributeReleaseService.fetch(id);
			if (release == null)
				throw new IllegalArgumentException();
			
			if (! release.getIdentity().equals(getIdentity()))
				throw new IllegalArgumentException("not allowed");
		}
		return release;
	}

}
