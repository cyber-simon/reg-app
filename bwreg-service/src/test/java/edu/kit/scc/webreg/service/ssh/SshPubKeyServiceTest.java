package edu.kit.scc.webreg.service.ssh;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddPackages;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.identity.JpaIdentityDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.test.EnableAutoWeldWithJpaSupport;
import edu.kit.scc.webreg.dao.test.JpaTestConfiguration;
import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.event.EventSubmitterImpl;

@EnableAutoWeldWithJpaSupport
@AddPackages(value = { JpaBaseDao.class, JpaIdentityDao.class }, recursively = true)
@AddBeanClasses({ SshPubKeyServiceImpl.class, EventSubmitterImpl.class })
@TestInstance(Lifecycle.PER_CLASS)
class SshPubKeyServiceTest {

	@Inject
	SshPubKeyService sshPubKeyService;

	@Inject
	IdentityDao identityDao;

	@Inject
	UserDao userDao;

	@Inject
	SshPubKeyDao sshPubKeyDao;

	@Produces
	public static JpaTestConfiguration produceJpaTestConfiguration() {
		return () -> AbstractBaseEntity.class.getPackageName();
	}

	@Produces
	public static HttpServletRequest produceHttpServletRequest() {
		return Mockito.mock(HttpServletRequest.class);
	}

	@Produces
	public static Logger produceLogger() {
		return NOPLogger.NOP_LOGGER;
	}

	@Nested
	public class FindByIdentityAndStatusWithRegs {

		@Test
		void returnsCorrectList() {
			IdentityEntity identity = createPersistedIdentityEntity("max_mustermann");
			UserEntity user = createPersistedUserEntity(4711, "Max", "Mustermann");
			SshPubKeyEntity sshPubKeyEntity = createPersistedSshPubKeyEntity(identity, user);

			List<SshPubKeyEntity> foundKeys = sshPubKeyService.findByIdentityAndStatusWithRegsAndUser(identity.getId(), SshPubKeyStatus.ACTIVE);

			assertThat(foundKeys).isNotNull().contains(sshPubKeyEntity);
		}

		@Test
		void returnsEmptyListIfNoneIsFound() {
			IdentityEntity identity = createPersistedIdentityEntity("max_mustermann");
			UserEntity user = createPersistedUserEntity(4711, "Max", "Mustermann");
			createPersistedSshPubKeyEntity(identity, user);

			List<SshPubKeyEntity> foundKeys = sshPubKeyService.findByIdentityAndStatusWithRegsAndUser(identity.getId(), SshPubKeyStatus.DELETED);

			assertThat(foundKeys).isEmpty();
		}

	}

	private SshPubKeyEntity createPersistedSshPubKeyEntity(IdentityEntity identity, UserEntity user) {
		return sshPubKeyDao.persist(createNewSshPubKeyEntity(identity, user));
	}

	private SshPubKeyEntity createNewSshPubKeyEntity(IdentityEntity identity, UserEntity user) {
		SshPubKeyEntity sshPubKeyEntity = new SshPubKeyEntity();
		sshPubKeyEntity.setIdentity(identity);
		sshPubKeyEntity.setUser(user);
		sshPubKeyEntity.setEncodedKey(UUID.randomUUID().toString());
		sshPubKeyEntity.setKeyStatus(SshPubKeyStatus.ACTIVE);
		return sshPubKeyEntity;
	}

	private IdentityEntity createPersistedIdentityEntity(String uniqueUsername) {
		return identityDao.persist(createNewIdentityEntity(uniqueUsername));
	}

	private IdentityEntity createNewIdentityEntity(String uniqueUsername) {
		IdentityEntity identity = new IdentityEntity();
		identity.setChosenLocalUsername(uniqueUsername);
		identity.setGeneratedLocalUsername(randomUUID().toString().replace("-", ""));
		return identity;
	}

	private UserEntity createPersistedUserEntity(Integer uidNumber, String firstName, String surname) {
		return userDao.persist(createNewUserEntity(uidNumber, firstName, surname));
	}

	private UserEntity createNewUserEntity(Integer uidNumber, String firstName, String surname) {
		UserEntity user = new UserEntity();
		user.setUidNumber(uidNumber);
		user.setGivenName(firstName);
		user.setSurName(surname);
		user.setEmail(String.format("%s.%s@unit.test", firstName, surname));
		return user;
	}

}
