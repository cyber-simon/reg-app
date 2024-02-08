package edu.kit.scc.webreg.bean.ar;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.attributes.AttributeReleaseService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.oidc.OidcFlowStateService;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ShowAttributeReleasesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private IdentityService identityService;

	@Inject
	private OidcFlowStateService flowStateService;

	@Inject
	private AttributeReleaseService attributeReleaseService;

	private IdentityEntity identity;
	private List<AttributeReleaseEntity> attributeReleaseList;
	
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
	
	public IdentityEntity getIdentity() {
		return identity;
	}

	public List<AttributeReleaseEntity> getAttributeReleaseList() {
		if (attributeReleaseList == null)
			attributeReleaseList = attributeReleaseService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(AttributeReleaseEntity_.id)),
					equal(AttributeReleaseEntity_.identity, getIdentity()));
		return attributeReleaseList;
	}

}
