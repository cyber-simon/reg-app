package edu.kit.scc.webreg.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddPackages;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.JpaIdentityDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.test.EnableAutoWeldWithJpaSupport;
import edu.kit.scc.webreg.dao.test.JpaTestConfiguration;
import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitterImpl;
import edu.kit.scc.webreg.service.RegistryService;

@EnableAutoWeldWithJpaSupport
@AddPackages(value = { JpaBaseDao.class, JpaIdentityDao.class }, recursively = true)
@AddBeanClasses({ RegistryServiceImpl.class, EventSubmitterImpl.class })
@TestInstance(Lifecycle.PER_CLASS)
class RegistryServiceImplTest {

	@Produces
	public static JpaTestConfiguration produceJpaTestConfiguration() {
		return () -> AbstractBaseEntity.class.getPackageName();
	}

	@Produces
	public static Logger produceLogger() {
		return NOPLogger.NOP_LOGGER;
	}

	@Inject
	RegistryDao registryDao;

	@Inject
	ServiceDao serviceDao;

	@Inject
	UserDao userDao;

	@Inject
	SamlIdpMetadataDao samlIdpMetadataDao;

	@Inject
	RegistryService registryService;

	@Nested
	public class FindByServiceAndStatusAndIDPGood {

		@Test
		public void findsMatchingRegistries() {
			Date lastStatusChange = Date.from(Instant.now().minusSeconds(1L));
			RegistryEntity registry = createRegistryEntity(lastStatusChange, SamlIdpMetadataEntityStatus.GOOD);

			List<RegistryEntity> foundRegistries = registryService.findByServiceAndStatusAndIDPGood("MySSN", RegistryStatus.ACTIVE,
					new Date(), 10);

			assertThat(foundRegistries).containsExactly(registry);
		}

		@Test
		public void ignoresRegistriesAssociatedWithUsersWithFaultyIdpState() {
			Date lastStatusChange = Date.from(Instant.now().minusSeconds(1L));
			createRegistryEntity(lastStatusChange, SamlIdpMetadataEntityStatus.FAULTY);

			List<RegistryEntity> foundRegistries = registryService.findByServiceAndStatusAndIDPGood("MySSN", RegistryStatus.ACTIVE,
					new Date(), 10);

			assertThat(foundRegistries).isEmpty();
		}

		private RegistryEntity createRegistryEntity(Date lastStatusChange, SamlIdpMetadataEntityStatus idpStatus) {
			SamlIdpMetadataEntity samlIdpMetadata = createPersistedSamlIdpMetadataEntity(idpStatus);
			UserEntity user = createPersistedSamlUserEntity(samlIdpMetadata);
			ServiceEntity service = createPersistedServiceEntity("MySSN");
			return createPersistedRegistryEntity(lastStatusChange, RegistryStatus.ACTIVE, service, user);
		}

	}

	private RegistryEntity createPersistedRegistryEntity(Date lastStatusChange, RegistryStatus registryStatus, ServiceEntity service,
			UserEntity user) {
		return registryDao.persist(createNewRegistryEntity(lastStatusChange, registryStatus, service, user));
	}

	private RegistryEntity createNewRegistryEntity(Date lastStatusChange, RegistryStatus registryStatus, ServiceEntity service,
			UserEntity user) {
		RegistryEntity registryEntity = new RegistryEntity();
		registryEntity.setLastStatusChange(lastStatusChange);
		registryEntity.setRegisterBean("registryRegisterBean");
		registryEntity.setRegistryStatus(registryStatus);
		registryEntity.setService(service);
		registryEntity.setUser(user);
		return registryEntity;
	}

	private ServiceEntity createPersistedServiceEntity(String shortName) {
		return serviceDao.persist(createNewServiceEntity(shortName));
	}

	private ServiceEntity createNewServiceEntity(String shortName) {
		ServiceEntity serviceEntity = new ServiceEntity();
		serviceEntity.setName("serviceName");
		serviceEntity.setShortName(shortName);
		serviceEntity.setRegisterBean("serviceRegisterBean");
		return serviceEntity;
	}

	private SamlUserEntity createPersistedSamlUserEntity(SamlIdpMetadataEntity samlIdpMetadataEntity) {
		return (SamlUserEntity) userDao.persist(createNewSamlUserEntity(samlIdpMetadataEntity));
	}

	private SamlUserEntity createNewSamlUserEntity(SamlIdpMetadataEntity samlIdpMetadataEntity) {
		SamlUserEntity samlUserEntity = new SamlUserEntity();
		samlUserEntity.setGivenName("Max");
		samlUserEntity.setIdp(samlIdpMetadataEntity);
		samlUserEntity.setSurName("Mustermann");
		samlUserEntity.setUidNumber(4711);
		return samlUserEntity;
	}

	private SamlIdpMetadataEntity createPersistedSamlIdpMetadataEntity(SamlIdpMetadataEntityStatus aqIdpStatus) {
		return samlIdpMetadataDao.persist(createNewSamlIdpMetadataEntity(aqIdpStatus));
	}

	private SamlIdpMetadataEntity createNewSamlIdpMetadataEntity(SamlIdpMetadataEntityStatus aqIdpStatus) {
		SamlIdpMetadataEntity samlIdpMetadataEntity = new SamlIdpMetadataEntity();
		samlIdpMetadataEntity.setAqIdpStatus(aqIdpStatus);
		return samlIdpMetadataEntity;
	}

}
