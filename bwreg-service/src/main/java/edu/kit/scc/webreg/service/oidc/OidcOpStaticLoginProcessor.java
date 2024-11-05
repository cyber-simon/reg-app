package edu.kit.scc.webreg.service.oidc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.oidc.ServiceOidcClientDao;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.ReleaseStatusType;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConsumerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.attribute.release.AttributeBuilder;
import edu.kit.scc.webreg.service.identity.IdentityAttributeResolver;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

@ApplicationScoped
public class OidcOpStaticLoginProcessor extends AbstractOidcOpLoginProcessor {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private ScriptingEnv scriptingEnv;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionSingleton;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private UserDao userDao;
	
	@Inject
	private ServiceOidcClientDao serviceOidcClientDao;

	@Inject
	private AttributeBuilder attributeBuilder;

	@Inject
	private IdentityAttributeResolver attributeResolver;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private SessionManager session;

	public boolean matches(OidcClientConsumerEntity clientConfig) {
		return true;
	}

	public String registerAuthRequest(OidcFlowStateEntity flowState, IdentityEntity identity)
			throws OidcAuthenticationException {
		logger.debug("Choosing standard attributes flow... scope is {}", flowState.getScope());
		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();
		List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

		if (serviceOidcClientList.size() == 0) {
			throw new OidcAuthenticationException("no script is connected to client configuration");
		}

		// User pref user
		// TODO Change to something more correct. Script must choose user from identity
		// ie.
		List<UserEntity> userList = userDao.findByIdentity(identity);
		UserEntity user = identity.getPrefUser();
		if (user == null)
			user = userList.get(0);

		Boolean wantsElevation = false;
		RegistryEntity registry = null;

		if (flowState.getAcrValues() != null
				&& flowState.getAcrValues().matches("(^|\\\s)https://refeds.org/profile/mfa($|\\\s)")) {
			wantsElevation = true;
		}

		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			if (serviceOidcClient.getWantsElevation() != null && serviceOidcClient.getWantsElevation()) {
				wantsElevation = true;
			}

			ServiceEntity service = serviceOidcClient.getService();

			if (service != null) {
				logger.debug("Service for RP found: {}", service);

				registry = registryDao.findByServiceAndIdentityAndStatus(service, identity, RegistryStatus.ACTIVE);

				if (registry != null) {
					List<Object> objectList = checkRules(registry.getUser(), service, registry);
					List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
					List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

					if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
						return "/user/check-access.xhtml?regId=" + registry.getId();
					}
				} else {
					registry = registryDao.findByServiceAndIdentityAndStatus(service, identity,
							RegistryStatus.LOST_ACCESS);

					if (registry != null) {
						logger.info("Registration for user {} and service {} in state LOST_ACCESS, checking again",
								registry.getUser().getEppn(), service.getName());
						List<Object> objectList = checkRules(registry.getUser(), service, registry);
						List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
						List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

						if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
							logger.info(
									"Registration for user {} and service {} in state LOST_ACCESS stays, redirecting to check page",
									registry.getUser().getEppn(), service.getName());
							return "/user/check-access.xhtml?regId=" + registry.getId();
						}
					} else {
						logger.info(
								"No active registration for identity {} and service {}, redirecting to register page",
								identity.getId(), service.getName());
						session.setOriginalRequestPath("/oidc/realms/" + flowState.getOpConfiguration().getRealm()
								+ "/protocol/openid-connect/auth/return");
						return "/user/register-service.xhtml?serviceId=" + service.getId();
					}
				}
			}

			if (serviceOidcClient.getRulePackage() != null) {
				/*
				 * There is an access rule for this oidc client. Check it.
				 */

				List<Object> objectList = checkRules(identity, serviceOidcClient.getRulePackage());
				List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
				List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

				if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
					return "/user/oidc-access-denied.xhtml?soidc=" + serviceOidcClient.getId();
				}
			}

			if (serviceOidcClient.getScript() != null) {
				List<String> unauthorizedList = knowledgeSessionSingleton
						.checkScriptAccess(serviceOidcClient.getScript(), identity);

				if (unauthorizedList.size() > 0) {
					return "/user/oidc-access-denied.xhtml?soidc=" + serviceOidcClient.getId();
				}

				wantsElevation |= evalTwoFa(serviceOidcClient.getScript(), identity, registry, flowState);
			}
		}

		if (wantsElevation) {
			long elevationTime = 5L * 60L * 1000L;
			if (appConfig.getConfigValue("elevation_time") != null) {
				elevationTime = Long.parseLong(appConfig.getConfigValue("elevation_time"));
			}

			if (session.getTwoFaElevation() == null
					|| (System.currentTimeMillis() - session.getTwoFaElevation().toEpochMilli()) > elevationTime) {
				// second factor is active for this service and web login
				// and user is not elevated yet
				session.setOriginalRequestPath("/oidc/realms/" + flowState.getOpConfiguration().getRealm()
						+ "/protocol/openid-connect/auth/return");
				return "/user/twofa-login.xhtml";
			}

		}

		/*
		 * need to calculate attributes here to show release data
		 */

		if (registry != null) {
			// Redefine user to match registry
			user = registry.getUser();
		}

		final AttributeReleaseEntity attributeRelease = attributeBuilder.requestAttributeRelease(clientConfig, identity);

		flowState.setValidUntil(new Date(System.currentTimeMillis() + (10L * 60L * 1000L)));
		flowState.setIdentity(identity);
		flowState.setRegistry(registry);
		flowState.setAttributeRelease(attributeRelease);

		resolveAttributes(attributeRelease, serviceOidcClientList, identity, user, registry, flowState, flowState.getOpConfiguration(), clientConfig);
		if (clientConfig.getGenericStore().containsKey("show_consent")
				&& clientConfig.getGenericStore().get("show_consent").equalsIgnoreCase("true")) {
			if (!ReleaseStatusType.GOOD.equals(attributeRelease.getReleaseStatus())) {
				// send client to attribute release page
				logger.debug("Attribute Release is not good, sending user to constent page");
				return "/user/attribute-release-oidc.xhtml?id=" + attributeRelease.getId();
			}
		} else {
			attributeRelease.setReleaseStatus(null);
		}
		
		String red = flowState.getRedirectUri() + "?code=" + flowState.getCode() + "&state=" + flowState.getState();
		logger.debug("Sending client to {}", red);
		return red;
	}

	public JSONObject buildAccessToken(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConsumerEntity consumerConfig, HttpServletResponse response) throws OidcAuthenticationException {

		if (!(consumerConfig instanceof OidcClientConfigurationEntity))
			throw new OidcAuthenticationException("This flow only supports legacy OidcClientConfigurationEntity");

		OidcClientConfigurationEntity clientConfig = (OidcClientConfigurationEntity) consumerConfig;

		final IdentityEntity identity = flowState.getIdentity();
		final AttributeReleaseEntity attributeRelease = flowState.getAttributeRelease();

		if (identity == null) {
			throw new OidcAuthenticationException("No identity attached to flow state.");
		}

		UserEntity user = identity.getPrefUser();
		if (user == null) {
			user = identity.getUsers().iterator().next();
		} 
		
		RegistryEntity registry = flowState.getRegistry();

		/*
		 * allow for no registry
		 */
//		if (registry == null) {
//			throw new OidcAuthenticationException("No registry attached to flow state.");
//		}

		List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

		JWTClaimsSet.Builder claimsBuilder = initClaimsBuilder(flowState).subject(user.getEppn());
		
		if (flowState.getScope() != null) {
			claimsBuilder.claim("scope", flowState.getScope());
		}

		buildStatement(claimsBuilder, "buildTokenStatement", serviceOidcClientList, identity, user, registry);

		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException(
							"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;

					invocable.invokeFunction("buildTokenStatement", scriptingEnv, claimsBuilder, user, registry,
							serviceOidcClient.getService(), logger, identity);
				} catch (NoSuchMethodException | ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			} else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}

		JWTClaimsSet claims = claimsBuilder.build();

		logger.debug("[OidcOpStaticLoginProcessor] claims before signing: " + claims.toJSONObject());

		Boolean shortIdTokenHeader = Boolean.TRUE;
		if (clientConfig.getGenericStore().containsKey("short_id_token_header"))
			shortIdTokenHeader = Boolean.parseBoolean(clientConfig.getGenericStore().get("long_access_token"));

		SignedJWT jwt = signClaims(opConfig, clientConfig, claims, shortIdTokenHeader);

		return finalizeTokenRespone(flowState, jwt).toJSONObject();
	}

	public JSONObject buildUserInfo(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConsumerEntity consumerConfig, HttpServletResponse response) throws OidcAuthenticationException {
		if (!(consumerConfig instanceof OidcClientConfigurationEntity))
			throw new OidcAuthenticationException("This flow only supports legacy OidcClientConfigurationEntity");

		OidcClientConfigurationEntity clientConfig = (OidcClientConfigurationEntity) consumerConfig;
		List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

		IdentityEntity identity = flowState.getIdentity();

		if (identity == null) {
			throw new OidcAuthenticationException("No identity attached to flow state.");
		}

		UserEntity user;
		if (identity.getUsers().size() == 1) {
			user = identity.getUsers().iterator().next();
		} else {
			user = identity.getPrefUser();
		}

		RegistryEntity registry = flowState.getRegistry();

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
		buildStatement(claimsBuilder, "buildClaimsStatement", serviceOidcClientList, identity, user, registry);

		UserInfo userInfo = new UserInfo(claimsBuilder.build());
		logger.debug("[OidcOpStaticLoginProcessor] userInfo Response: " + userInfo.toJSONObject());
		return userInfo.toJSONObject();
	}

	private void resolveAttributes(AttributeReleaseEntity attributeRelease,
			List<ServiceOidcClientEntity> serviceOidcClientList, IdentityEntity identity, UserEntity user,
			RegistryEntity registry, OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConsumerEntity consumerConfig) throws OidcAuthenticationException {

		attributeRelease.setValuesToDelete(new HashSet<>(attributeRelease.getValues()));

		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException(
							"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;

					invocable.invokeFunction("resolveAttributes", scriptingEnv, attributeBuilder, attributeResolver,
							attributeRelease, identity, user, registry, logger, flowState, consumerConfig, opConfig);
				} catch (NoSuchMethodException e) {
					logger.info("Skipping resolveAttributes method. It's not defined.");
				} catch (ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			} else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}

		attributeRelease.getValuesToDelete().stream().forEach(v -> attributeBuilder.deleteValue(v));
	}

	private void buildStatement(JWTClaimsSet.Builder claimsBuilder, String methodName,
			List<ServiceOidcClientEntity> serviceOidcClientList, IdentityEntity identity, UserEntity user,
			RegistryEntity registry) throws OidcAuthenticationException {

		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException(
							"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;

					invocable.invokeFunction(methodName, scriptingEnv, claimsBuilder, user, registry,
							serviceOidcClient.getService(), logger, identity);
				} catch (NoSuchMethodException e) {
					logger.info("Skipping " + methodName + " method. It's not defined.");
				} catch (ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			} else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}
	}

	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		return knowledgeSessionSingleton.checkServiceAccessRule(user, service, registry, "user-self", false);
	}

	private List<Object> checkRules(IdentityEntity identity, BusinessRulePackageEntity rulePackage) {
		return knowledgeSessionSingleton.checkIdentityRule(rulePackage, identity);
	}

	private List<OverrideAccess> extractOverideAccess(List<Object> objectList) {
		List<OverrideAccess> returnList = new ArrayList<OverrideAccess>();

		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				returnList.add((OverrideAccess) o);
			}
		}

		return returnList;
	}

	private List<UnauthorizedUser> extractUnauthorizedUser(List<Object> objectList) {
		List<UnauthorizedUser> returnList = new ArrayList<UnauthorizedUser>();

		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				returnList.add((UnauthorizedUser) o);
			}
		}

		return returnList;
	}

	private boolean evalTwoFa(ScriptEntity scriptEntity, IdentityEntity identity, RegistryEntity registry,
			OidcFlowStateEntity flowState) {
		ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

		if (engine == null) {
			logger.warn("Script Engine is null, cannot executes script");
			return false;
		}

		try {
			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;

			Object object = invocable.invokeFunction("evalTwoFa", scriptingEnv, identity, registry, flowState, logger);

			if (object instanceof Boolean && ((Boolean) object))
				return true;
		} catch (ScriptException e) {
			logger.warn("Script execution failed. Continue with other scripts.", e);
		} catch (NoSuchMethodException e) {
			logger.debug("No evalTwoFa method in script. Assuming match false");
		}
		return false;
	}
}
