package edu.kit.scc.webreg.dao.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.test.EnableAutoWeldWithJpaSupport;
import edu.kit.scc.webreg.dao.test.JpaTestConfiguration;
import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;

@EnableAutoWeldWithJpaSupport
@AddBeanClasses({ JpaSamlUserDao.class, JpaSamlIdpMetadataDao.class })
@TestInstance(Lifecycle.PER_CLASS)
class JpaSamlUserDaoTest {

	@Inject
	JpaSamlUserDao daoUnderTest;

	@Inject
	SamlIdpMetadataDao samlIdpMetadataDao;

	@Inject
	EntityManager entityManager;

	@Produces
	public JpaTestConfiguration produceJpaTestConfiguration() {
		return () -> AbstractBaseEntity.class.getPackageName();
	}

	@Nested
	public class FindByPersistent {

		//		@Test
		public void ignoresCaseForPersistentId() {
			SamlUserEntity persistedSamlUserEntity = createPersistedSamlUserEntity("cRAzY!!");

			SamlUserEntity actual = daoUnderTest.findByPersistent(persistedSamlUserEntity.getPersistentSpId(),
					persistedSamlUserEntity.getIdp().getEntityId(), "Crazy!!");

			assertThat(actual).isEqualTo(persistedSamlUserEntity);
		}

	}

	private SamlUserEntity createPersistedSamlUserEntity(String persistentId) {
		return daoUnderTest.persist(createNewSamlUserEntity(persistentId, createPersistedSamlIdpMetadataEntity()));
	}

	private SamlUserEntity createNewSamlUserEntity(String persistentId, SamlIdpMetadataEntity samlIdpMetadataEntity) {
		SamlUserEntity samlUserEntity = new SamlUserEntity();
		samlUserEntity.setIdp(samlIdpMetadataEntity);
		samlUserEntity.setPersistentId(persistentId);
		samlUserEntity.setPersistentSpId("333");
		samlUserEntity.setUidNumber(123456789);
		return samlUserEntity;
	}

	private SamlIdpMetadataEntity createPersistedSamlIdpMetadataEntity() {
		return samlIdpMetadataDao.persist(createNewSamlIdpMetadataEntity());
	}

	private SamlIdpMetadataEntity createNewSamlIdpMetadataEntity() {
		SamlIdpMetadataEntity samlIdpMetadataEntity = new SamlIdpMetadataEntity();
		samlIdpMetadataEntity.setEntityId("4711");
		return samlIdpMetadataEntity;
	}

}
