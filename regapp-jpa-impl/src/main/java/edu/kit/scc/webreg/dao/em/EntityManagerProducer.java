package edu.kit.scc.webreg.dao.em;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class EntityManagerProducer {

	@PersistenceContext
	private EntityManager em;

	@Produces
	public EntityManager produce() {
		return em;
	}
}