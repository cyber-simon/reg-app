package edu.kit.scc.webreg.service.identity;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.hook.UserUpdateHookException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IdentityCreater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private IdentityDao dao;

	@Inject
	private IdentityScriptingEnv scriptingEnv;

	@Inject
	private OidcRpConfigurationDao rpConfigurationDao;
	
	@Inject
	private SamlIdpMetadataDao samlIdpMetadataDao;
	
	public IdentityEntity preMatchIdentity(UserEntity user, Map<String, List<Object>> attributeMap) {
		if (user instanceof OidcUserEntity) {
			OidcUserEntity oidcUser = ((OidcUserEntity) user);
			OidcRpConfigurationEntity issuer = rpConfigurationDao.fetch(oidcUser.getIssuer().getId());
			if (issuer.getGenericStore().containsKey("identity_matcher_script")) {
				try {
					Invocable invocable = resolveScript(issuer.getGenericStore().get("identity_matcher_script"));
					Object o = invocable.invokeFunction("resolveIdentity", scriptingEnv, user, attributeMap, logger);
					if (o instanceof IdentityEntity) {
						return (IdentityEntity) o;
					}
				} catch (NoSuchMethodException e) {
					logger.info("No resolveIdentity Method. Skipping execution.");
				} catch (ScriptException | UserUpdateHookException e) {
					logger.info("Script error on resolveIdentity", e);
				}
			}
		}
		else if (user instanceof SamlUserEntity) {
			SamlUserEntity samlUser = (SamlUserEntity) user;
			SamlIdpMetadataEntity idp = samlIdpMetadataDao.fetch(samlUser.getIdp().getId());
			if (idp.getGenericStore().containsKey("identity_matcher_script")) {
				try {
					Invocable invocable = resolveScript(idp.getGenericStore().get("identity_matcher_script"));
					Object o = invocable.invokeFunction("resolveIdentity", scriptingEnv, user, attributeMap, logger);
					if (o instanceof IdentityEntity) {
						return (IdentityEntity) o;
					}
				} catch (NoSuchMethodException e) {
					logger.info("No resolveIdentity Method. Skipping execution.");
				} catch (ScriptException | UserUpdateHookException e) {
					logger.info("Script error on resolveIdentity", e);
				}
			}
		}
		return null;
	}

	public IdentityEntity preCreateIdentity() {

		IdentityEntity identity = dao.createNew();
		identity = dao.persist(identity);

		identity.setTwoFaUserId("idty-" + identity.getId());
		identity.setTwoFaUserName(UUID.randomUUID().toString());

		return identity;
	}

	public void postCreateIdentity(IdentityEntity identity, UserEntity user) {
		if (identity.getPrefUser() == null) {
			identity.setPrefUser(user);
		}

		if (identity.getUidNumber() == null) {
			identity.setUidNumber(user.getUidNumber());
		}

		if (identity.getGeneratedLocalUsername() == null) {
			logger.debug("No local generated username for identity {}. Generating one...", user.getIdentity());
			String generatedName = randomAlphabetic(3).toLowerCase() + randomNumeric(4);
			logger.debug("Generated username for identity {}: {}", user.getIdentity(), generatedName);
			while (dao.find(equal("generatedLocalUsername", generatedName)) != null) {
				generatedName = randomAlphabetic(3).toLowerCase() + randomNumeric(4);
				logger.debug("Generated username is already taken. Try again for identity {}: {}", user.getIdentity(),
						generatedName);
			}
			user.getIdentity().setGeneratedLocalUsername(generatedName);
		}
	}

	private Invocable resolveScript(String scriptName) throws UserUpdateHookException {
		try {
			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);

			if (scriptEntity == null)
				throw new UserUpdateHookException("identity matcher not configured properly. script is missing.");

			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new UserUpdateHookException("identity matcher not configured properly. engine not found: "
							+ scriptEntity.getScriptEngine());

				engine.eval(scriptEntity.getScript());

				Invocable invocable = (Invocable) engine;

				return invocable;
			} else {
				throw new UserUpdateHookException("unkown script type: " + scriptEntity.getScriptType());
			}
		} catch (ScriptException e) {
			throw new UserUpdateHookException(e);
		}
	}

}
