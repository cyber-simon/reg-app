package edu.kit.scc.webreg.service.oidc;

import java.util.Date;

import org.slf4j.Logger;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.ReleaseStatusType;
import edu.kit.scc.webreg.entity.attribute.value.PairwiseIdentifierValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.attribute.release.AttributeReleaseHandler;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

@ApplicationScoped
public class OidcOpScopeLoginProcessor extends AbstractOidcOpLoginProcessor {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeReleaseHandler attributeReleaseHandler;

	public boolean matches(OidcClientConfigurationEntity clientConfig) {
		return (clientConfig.getGenericStore().containsKey("new_attributes")
				&& clientConfig.getGenericStore().get("new_attributes").equalsIgnoreCase("true") ? true : false);
	}

	public String registerAuthRequest(OidcFlowStateEntity flowState, IdentityEntity identity)
			throws OidcAuthenticationException {
		logger.debug("Choosing new attributes flow... scope is {}", flowState.getScope());
		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();

		AttributeReleaseEntity attributeRelease = attributeReleaseHandler.requestAttributeRelease(clientConfig,
				identity);
		Boolean changed = attributeReleaseHandler.calculateOidcValues(attributeRelease, flowState);
		if (changed && ReleaseStatusType.GOOD.equals(attributeRelease.getReleaseStatus())) {
			attributeRelease.setReleaseStatus(ReleaseStatusType.DIRTY);
		}
		
		if (ReleaseStatusType.NEW.equals(attributeRelease.getReleaseStatus())) {
			logger.debug("Attribute Release is new, sending user to constent page");
			return "/user/attribute-release-oidc.xhtml?id=" + attributeRelease.getId();
		} else if (ReleaseStatusType.DIRTY.equals(attributeRelease.getReleaseStatus())) {
			logger.debug("Attribute Release is dirty, sending user to constent page");
			return "/user/attribute-release-oidc.xhtml?id=" + attributeRelease.getId();
		} else if (ReleaseStatusType.REVOKED.equals(attributeRelease.getReleaseStatus())) {
			logger.debug("Attribute Release is revoeked, sending user to constent page");
			return "/user/attribute-release-oidc.xhtml?id=" + attributeRelease.getId();
		} else if (ReleaseStatusType.REJECTED.equals(attributeRelease.getReleaseStatus())) {
			logger.debug("Attribute Release is rejected, sending user to constent page");
			return "/user/attribute-release-oidc.xhtml?id=" + attributeRelease.getId();
		} else if (ReleaseStatusType.GOOD.equals(attributeRelease.getReleaseStatus())) {
			flowState.setValidUntil(new Date(System.currentTimeMillis() + (10L * 60L * 1000L)));
			flowState.setIdentity(identity);
			flowState.setAttributeRelease(attributeRelease);

			String red = flowState.getRedirectUri() + "?code=" + flowState.getCode() + "&state=" + flowState.getState();
			logger.debug("Sending client to {}", red);
			return red;
		} else {
			throw new OidcAuthenticationException("No message yet");
		}
	}

	public JSONObject buildAccessToken(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConfigurationEntity clientConfig, HttpServletResponse response) throws OidcAuthenticationException {
		IdentityEntity identity = flowState.getIdentity();
		AttributeReleaseEntity attributeRelease = attributeReleaseHandler.requestAttributeRelease(clientConfig,
				identity);
		
		if (! ReleaseStatusType.GOOD.equals(attributeRelease.getReleaseStatus())) {
			return sendError(OAuth2Error.ACCESS_DENIED, response);
		}
		
		JWTClaimsSet.Builder claimsBuilder = initClaimsBuilder(flowState);

		for (ValueEntity value : attributeRelease.getValues()) {
			if (value.getAttribute().getName().equals("sub")) {
				claimsBuilder.subject(((PairwiseIdentifierValueEntity) value).getValueIdentifier() + "@"
						+ ((PairwiseIdentifierValueEntity) value).getValueScope());
			}
		}

		JWTClaimsSet claims = claimsBuilder.build();

		logger.debug("[OidcOpScopeLoginProcessor] claims before signing: " + claims.toJSONObject());

		SignedJWT jwt = signClaims(opConfig, clientConfig, claims);

		return finalizeTokenRespone(flowState, jwt).toJSONObject();
	}
	
	public JSONObject buildUserInfo(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConfigurationEntity clientConfig, HttpServletResponse response) throws OidcAuthenticationException {
		IdentityEntity identity = flowState.getIdentity();
		AttributeReleaseEntity attributeRelease = attributeReleaseHandler.requestAttributeRelease(clientConfig,
				identity);

		if (! ReleaseStatusType.GOOD.equals(attributeRelease.getReleaseStatus())) {
			return sendError(OAuth2Error.ACCESS_DENIED, response);
		}

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
		
		for (ValueEntity value : attributeRelease.getValues()) {
			if (value instanceof PairwiseIdentifierValueEntity) {
				claimsBuilder.subject(((PairwiseIdentifierValueEntity) value).getValueIdentifier() + "@"
						+ ((PairwiseIdentifierValueEntity) value).getValueScope());
			}
			else if (value instanceof StringValueEntity) {
				claimsBuilder.claim(value.getAttribute().getName(), ((StringValueEntity) value).getValueString());
			}
		}

		UserInfo userInfo = new UserInfo(claimsBuilder.build());
		logger.debug("[OidcOpScopeLoginProcessor] userInfo Response: " + userInfo.toJSONObject());
		return userInfo.toJSONObject();
	}
}
