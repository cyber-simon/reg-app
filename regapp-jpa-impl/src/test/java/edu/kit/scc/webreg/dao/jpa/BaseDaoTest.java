package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.NullOrder.NULL_FIRST;
import static edu.kit.scc.webreg.dao.ops.NullOrder.NULL_LAST;
import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;
import static edu.kit.scc.webreg.dao.ops.SortBy.descendingBy;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.identity.JpaIdentityDao;
import edu.kit.scc.webreg.dao.test.EnableAutoWeldWithJpaSupport;
import edu.kit.scc.webreg.dao.test.JpaTestConfiguration;
import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity_;

@EnableAutoWeldWithJpaSupport
@AddBeanClasses({ JpaIdentityDao.class, JpaUserDao.class })
@TestInstance(Lifecycle.PER_CLASS)
class BaseDaoTest {

	@Inject
	IdentityDao identityDao;

	@Inject
	EntityManager entityManager;

	@Produces
	public JpaTestConfiguration produceJpaTestConfiguration() {
		return () -> AbstractBaseEntity.class.getPackageName();
	}

	@Nested
	public class CountAll {

		@Test
		public void returnsCorrectNumber() {
			createPersistedIdentityEntity("chosen local username");

			Integer actual = identityDao.countAll(null).intValue();

			assertThat(actual).isEqualTo(1);
		}

	}

	@Nested
	public class FindAll {

		@Test
		public void returnsCorrectList() {
			IdentityEntity persistedEntity1 = createPersistedIdentityEntity("me");
			IdentityEntity persistedEntity2 = createPersistedIdentityEntity("you");

			List<IdentityEntity> foundEntities = identityDao.findAll();

			assertThat(foundEntities).containsExactly(persistedEntity1, persistedEntity2);
		}

		@Test
		public void respectsNullFirstOptionSortingInAscendingOrder() {
			IdentityEntity newEntity1 = createNewIdentityEntity("me");
			newEntity1.setUidNumber(0);
			IdentityEntity persistedEntity1 = identityDao.persist(newEntity1);
			IdentityEntity persistedEntity2 = createPersistedIdentityEntity("you");

			List<IdentityEntity> foundEntities = identityDao.findAll(unlimited(), ascendingBy(IdentityEntity_.uidNumber, NULL_FIRST), null);

			assertThat(foundEntities).containsExactly(persistedEntity2, persistedEntity1);
		}

		@Test
		public void respectsNullFirstOptionSortingInDescendingOrder() {
			IdentityEntity newEntity1 = createNewIdentityEntity("me");
			newEntity1.setUidNumber(0);
			IdentityEntity persistedEntity1 = identityDao.persist(newEntity1);
			IdentityEntity persistedEntity2 = createPersistedIdentityEntity("you");

			List<IdentityEntity> foundEntities = identityDao.findAll(unlimited(), descendingBy(IdentityEntity_.uidNumber, NULL_FIRST),
					null);

			assertThat(foundEntities).containsExactly(persistedEntity2, persistedEntity1);
		}

		@Test
		public void respectsNullLastOptionSortingInAscendingOrder() {
			IdentityEntity newEntity1 = createNewIdentityEntity("me");
			newEntity1.setUidNumber(0);
			IdentityEntity persistedEntity1 = identityDao.persist(newEntity1);
			IdentityEntity persistedEntity2 = createPersistedIdentityEntity("you");

			List<IdentityEntity> foundEntities = identityDao.findAll(unlimited(), ascendingBy(IdentityEntity_.uidNumber, NULL_LAST), null);

			assertThat(foundEntities).containsExactly(persistedEntity1, persistedEntity2);
		}

		@Test
		public void respectsNullLastOptionSortingInDescendingOrder() {
			IdentityEntity newEntity1 = createNewIdentityEntity("me");
			newEntity1.setUidNumber(0);
			IdentityEntity persistedEntity1 = identityDao.persist(newEntity1);
			IdentityEntity persistedEntity2 = createPersistedIdentityEntity("you");

			List<IdentityEntity> foundEntities = identityDao.findAll(unlimited(), descendingBy(IdentityEntity_.uidNumber, NULL_LAST), null);

			assertThat(foundEntities).containsExactly(persistedEntity1, persistedEntity2);
		}

	}

	@Nested
	public class Persist {

		@Test
		public void persistsEntity() {
			IdentityEntity newEntity = createNewIdentityEntity("chosen local username");

			IdentityEntity persistedEntity = identityDao.persist(newEntity);

			assertThat(persistedEntity).isNotNull();
			assertSoftly(softly -> {
				softly.assertThat(persistedEntity.getId()).isNotNull();
				softly.assertThat(persistedEntity.getChosenLocalUsername()).isEqualTo(newEntity.getChosenLocalUsername());
			});
		}

		@Test
		public void setsVersion() {
			IdentityEntity newEntity = createNewIdentityEntity("chosen local username");

			IdentityEntity persistedEntity = identityDao.persist(newEntity);

			assertThat(persistedEntity).isNotNull();
			assertThat(persistedEntity.getVersion()).isNotNull().isNotNegative();
		}

		@Test
		public void setsUpdatedAt() {
			IdentityEntity newEntity = createNewIdentityEntity("chosen local username");

			IdentityEntity persistedEntity = identityDao.persist(newEntity);

			assertThat(persistedEntity).isNotNull();
			assertThat(persistedEntity.getUpdatedAt()).isNotNull();
		}

		@Test
		public void setsCreatedAt() {
			IdentityEntity newEntity = createNewIdentityEntity("chosen local username");

			IdentityEntity persistedEntity = identityDao.persist(newEntity);

			assertThat(persistedEntity).isNotNull();
			assertThat(persistedEntity.getCreatedAt()).isNotNull();
		}

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

}
