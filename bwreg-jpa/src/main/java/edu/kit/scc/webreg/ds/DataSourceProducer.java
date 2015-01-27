package edu.kit.scc.webreg.ds;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ApplicationScoped
public class DataSourceProducer {

    @PersistenceContext(unitName = "bwidm")
	private EntityManager em;

    @Produces
    @DefaultDatasource
	public EntityManager getEntityManager() {
		return em;
	}   
}
