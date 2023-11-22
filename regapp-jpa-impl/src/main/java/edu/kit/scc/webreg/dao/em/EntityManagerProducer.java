package edu.kit.scc.webreg.dao.em;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class EntityManagerProducer {

	@PersistenceContext
	private EntityManager em;

	@Produces
	public EntityManager produce() {
		return em;
	}
}