package edu.kit.scc.webreg.dao;

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.ServiceEntity;

public interface StatisticsDao {

	Map<Long, Long> countUsersPerIdp();

	List<Object> countUsersPerService();

	List<Object> countUsersPerIdpAndService(ServiceEntity service);

	List<Object> countUsersPerMonth();

	List<Object> countRegistriesPerMonthAndService();

}
