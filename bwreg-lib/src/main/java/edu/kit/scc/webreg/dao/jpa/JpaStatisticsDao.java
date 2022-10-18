package edu.kit.scc.webreg.dao.jpa;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import edu.kit.scc.webreg.dao.StatisticsDao;
import edu.kit.scc.webreg.ds.DefaultDatasource;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Named
@ApplicationScoped
public class JpaStatisticsDao implements Serializable, StatisticsDao {

	private static final long serialVersionUID = 1L;

    @Inject
    @DefaultDatasource
    protected EntityManager em;
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<Object> countUsersPerIdp() {
		return em.createQuery("select new list(count(u) as cnt, i) from UserEntity u join u.idp i group by i order by cnt desc")
				.getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<Object> countUsersPerMonth() {
		return em.createQuery("select new list(count(u) as cnt, year(u.createdAt) as y, month(u.createdAt) as m) from UserEntity u "
				+ "group by year(u.createdAt), month(u.createdAt) order by y, m")
				.getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<Object> countRegistriesPerMonthAndService() {
		return em.createQuery("select new list(count(r) as cnt, year(r.agreedTime) as y, month(r.agreedTime) as m, s) "
				+ "from RegistryEntity r "
				+ "join r.service s "
				+ "group by s, year(r.agreedTime), month(r.agreedTime) order by s, y, m")
				.getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<Object> countUsersPerIdpAndService(ServiceEntity service) {
		return em.createQuery("select new list(count(r) as cnt, i) from RegistryEntity r join r.user u"
				+ " join u.idp i where r.service = :service and r.registryStatus = :status group by i order by cnt desc")
				.setParameter("status", RegistryStatus.ACTIVE)
				.setParameter("service", service)
				.getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<Object> countUsersPerService() {
		return em.createQuery("select new list(count(r) as cnt, s) from RegistryEntity r join r.service s where r.registryStatus = :status group by s order by cnt desc")
				.setParameter("status", RegistryStatus.ACTIVE).getResultList();
	}
	
}
