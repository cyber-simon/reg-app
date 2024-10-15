package edu.kit.scc.webreg.bean.ar;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.entity.SamlAuthnRequestEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity_;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.SamlAuthnRequestService;
import edu.kit.scc.webreg.service.attributes.AttributeReleaseService;
import edu.kit.scc.webreg.service.attributes.ValueService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class SamlAttributeReleaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private IdentityService identityService;

	@Inject
	private SamlAuthnRequestService authnRequestService;

	@Inject
	private AttributeReleaseService attributeReleaseService;

	@Inject
	private ValueService valueService;

	private Long id;
	private IdentityEntity identity;
	private SamlAuthnRequestEntity authnRequest;
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

			if (session.getAuthnRequestId() == null) {
				throw new IllegalStateException("There is no AuthnRequest attached to actual session");
			}
			authnRequest = authnRequestService.findByIdWithAttrs(session.getAuthnRequestId());
			if (authnRequest == null) {
				throw new IllegalStateException("Corresponding AuthnRequest not found.");
			}

			attributeRelease = attributeReleaseService.findByIdWithAttrs(authnRequest.getAttributeRelease().getId());
			if (!attributeRelease.getIdentity().equals(identity)) {
				throw new IllegalStateException("Not authorised.");
			}

			attributeRelease = attributeReleaseService.findByIdWithAttrs(authnRequest.getAttributeRelease().getId(),
					AttributeReleaseEntity_.values);
		}
	}

	public void accept() {
		attributeRelease = attributeReleaseService.accept(attributeRelease);

		String red = "/saml/idp/redirect/response";
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect(red);
		} catch (IOException e) {
		}
	}

	public void reject() {
		attributeRelease = attributeReleaseService.reject(attributeRelease);

		String red = "/saml/idp/redirect/response";
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

	public SamlAuthnRequestEntity getAuthnRequest() {
		return authnRequest;
	}

}
