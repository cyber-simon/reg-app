package edu.kit.scc.webreg.dao.test;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.EntityManager;

import org.jnp.server.NamingBeanImpl;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.arjuna.ats.jta.utils.JNDIManager;

public class JtaEnvironmentExtension
		implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

	private NamingBeanImpl NAMING_BEAN;
	private int nestedClassesDepth = 0;

	@Override
	public synchronized void beforeAll(ExtensionContext context) throws Exception {
		if (nestedClassesDepth == 0) {
			NAMING_BEAN = new NamingBeanImpl();
			NAMING_BEAN.start();

			JNDIManager.bindJTAImplementation();
			TransactionalConnectionProvider.bindDataSource();
		}
		nestedClassesDepth = nestedClassesDepth + 1;
	}

	@Override
	public synchronized void afterAll(ExtensionContext context) throws Exception {
		nestedClassesDepth = nestedClassesDepth - 1;
		if (nestedClassesDepth == 0) {
			NAMING_BEAN.stop();
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		getEntityManager().getTransaction().begin();
	}

	private EntityManager getEntityManager() {
		return CDI.current().select(EntityManager.class).get();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		getEntityManager().getTransaction().rollback();
	}

}
