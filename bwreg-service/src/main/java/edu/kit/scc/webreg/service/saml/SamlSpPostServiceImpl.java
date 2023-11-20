package edu.kit.scc.webreg.service.saml;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.impl.UserUpdater;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SamlSpPostServiceImpl implements SamlSpPostService {

	@Inject
	private Logger logger;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private UserLoginInfoDao userLoginInfoDao;

	@Inject
	private UserUpdater userUpdater;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private Saml2DecoderService saml2DecoderService;

	@Inject
	private Saml2AssertionService saml2AssertionService;

	@Inject
	private SessionManager session;

	@Override
	@RetryTransaction
	public void consumePost(HttpServletRequest request, HttpServletResponse response,
			SamlSpConfigurationEntity spConfig, StringBuffer debugLog) throws Exception {

		SamlIdpMetadataEntity idpEntity = idpDao.fetch(session.getIdpId());
		EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(idpEntity.getEntityDescriptor(),
				EntityDescriptor.class);

		if (debugLog != null) {
			debugLog.append("Resolved IDP for login: ").append(idpEntity.getEntityId()).append("\n");
		}

		Assertion assertion = null;
		Response samlResponse = null;
		SamlIdentifier samlIdentifier;
		try {

			if (debugLog != null) {
				debugLog.append("Decoding SAML Response...\n");
			}

			samlResponse = saml2DecoderService.decodePostMessage(request);

			if (logger.isTraceEnabled())
				logger.trace("{}", samlHelper.prettyPrint(samlResponse));

			if (debugLog != null) {
				debugLog.append("Decoding SAML Assertion...\n");
			}

			assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor,
					spConfig);

			if (logger.isTraceEnabled())
				logger.trace("{}", samlHelper.prettyPrint(assertion));

			if (debugLog != null) {
				debugLog.append("Extract Persistent NameID...\n");
			}

			samlIdentifier = saml2AssertionService.extractPersistentId(idpEntity, assertion, spConfig, debugLog);

			if (debugLog != null) {
				debugLog.append("Resulting IDs (persistent, pairwise, subject): (")
						.append(samlIdentifier.getPersistentId()).append(", ").append(samlIdentifier.getPairwiseId())
						.append(", ").append(samlIdentifier.getSubjectId()).append(")\n");
			}

		} catch (Exception e1) {
			/*
			 * Catch Exception here for a probably faulty IDP. Register Exception and
			 * rethrow.
			 */
			if (!SamlIdpMetadataEntityStatus.FAULTY.equals(idpEntity.getIdIdpStatus())) {
				idpEntity.setIdIdpStatus(SamlIdpMetadataEntityStatus.FAULTY);
				idpEntity.setLastIdStatusChange(new Date());
			}
			throw e1;
		} finally {
			if (debugLog != null) {
				if (samlResponse != null) {
					debugLog.append("\n\nSAML Response:\n\n").append(samlHelper.prettyPrint(samlResponse));
				}

				if (assertion != null) {
					debugLog.append("\n\nSAML Assertion:\n\n").append(samlHelper.prettyPrint(assertion));
				}
			}
		}

		if (!SamlIdpMetadataEntityStatus.GOOD.equals(idpEntity.getIdIdpStatus())) {
			idpEntity.setIdIdpStatus(SamlIdpMetadataEntityStatus.GOOD);
			idpEntity.setLastIdStatusChange(new Date());
		}

		Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);

		SamlUserEntity user = saml2AssertionService.resolveUser(samlIdentifier, idpEntity, spConfig.getEntityId(),
				null);

		if (user != null) {
			MDC.put("userId", "" + user.getId());
		}

		if (user == null) {
			logger.info("New User detected, sending to register Page");

			// Store SAML Data temporarily in Session
			logger.debug("Storing relevant SAML data in session");
			session.setSamlIdentifier(samlIdentifier);
			session.setAttributeMap(attributeMap);

			if (debugLog != null) {
				request.setAttribute("_debugLogExtraRedirect", "/register/register.xhtml");
				return;
			} else {
				response.sendRedirect("/register/register.xhtml");
				return;
			}
		}

		logger.debug("Updating user {}", user.getId());

		saml2AssertionService.updateUserIdentifier(samlIdentifier, user, spConfig.getEntityId(), debugLog);

		try {
			user = userUpdater.updateUser(user, assertion, "web-sso", request.getLocalName());
		} catch (UserUpdateException e) {
			logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
			throw new SamlAuthenticationException(e.getMessage());
		}

		session.setIdentityId(user.getIdentity().getId());
		session.setLoginTime(Instant.now());
		session.setTheme(user.getTheme());
		session.getLoggedInUserList().add(user.getId());

		UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
		loginInfo.setUser(user);
		loginInfo.setLoginDate(new Date());
		loginInfo.setLoginMethod(UserLoginMethod.HOME_ORG);
		loginInfo.setLoginStatus(UserLoginInfoStatus.SUCCESS);
		loginInfo.setFrom(request.getRemoteAddr());
		loginInfo = userLoginInfoDao.persist(loginInfo);

		if (debugLog != null) {
			return;
		} else if (session.getOriginalRequestPath() != null) {
			String orig = session.getOriginalRequestPath();
			session.setOriginalRequestPath(null);
			response.sendRedirect(orig);
		} else
			response.sendRedirect("/index.xhtml");

	}
}
