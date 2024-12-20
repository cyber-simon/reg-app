package edu.kit.scc.webreg.bean.ar;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity_;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity_;
import edu.kit.scc.webreg.service.attributes.AttributeReleaseService;
import edu.kit.scc.webreg.service.attributes.ValueService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.oidc.OidcFlowStateService;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class OidcAttributeReleaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private IdentityService identityService;

	@Inject
	private OidcFlowStateService flowStateService;

	@Inject
	private AttributeReleaseService attributeReleaseService;

	@Inject
	private ValueService valueService;

	private Long id;
	private IdentityEntity identity;
	private OidcFlowStateEntity flowState;
	private AttributeReleaseEntity attributeRelease;
	private List<ValueEntity> valueList;

	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			if (session.getIdentityId() != null) {
				identity = identityService.fetch(session.getIdentityId());
			}

			if (identity == null) {
				throw new IllegalStateException("User ID missing.");
			}

			if (session.getOidcFlowStateId() == null) {
				throw new IllegalStateException("There is not flow state attached to actual session");
			}
			flowState = flowStateService.findByIdWithAttrs(session.getOidcFlowStateId(),
					OidcFlowStateEntity_.clientConfiguration);
			if (flowState == null) {
				throw new IllegalStateException("Corresponding flow state not found.");
			}

			attributeRelease = attributeReleaseService.findByIdWithAttrs(flowState.getAttributeRelease().getId());
			if (!attributeRelease.getIdentity().equals(identity)) {
				throw new IllegalStateException("Not authorised.");
			}

			attributeRelease = attributeReleaseService.findByIdWithAttrs(flowState.getAttributeRelease().getId(),
					AttributeReleaseEntity_.values);
		}
	}

	public void accept() {
		attributeRelease = attributeReleaseService.accept(attributeRelease, flowState, identity);
		flowState = flowStateService.fetch(flowState.getId());

		String red = flowState.getRedirectUri() + "?code=" + flowState.getCode() + "&state=" + flowState.getState();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(red);
		} catch (IOException e) {
		}
	}

	public void reject() {
		attributeRelease = attributeReleaseService.reject(attributeRelease, flowState, identity);
		flowState = flowStateService.fetch(flowState.getId());

		String red = flowState.getRedirectUri()
				+ "?error=access_denied&error_description=end-user%20denied%20the%20authorization%20request";
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(red);
		} catch (IOException e) {
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public OidcFlowStateEntity getFlowState() {
		return flowState;
	}

	public List<ValueEntity> getValueList() {
		if (valueList == null) {
			valueList = valueService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(ValueEntity_.id)),
					equal(ValueEntity_.attributeRelease, getAttributeRelease()),
					StringListValueEntity_.valueList);
		}
		return valueList;
	}

	public AttributeReleaseEntity getAttributeRelease() {
		return attributeRelease;
	}

}
