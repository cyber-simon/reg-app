package edu.kit.scc.webreg.service;

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

public interface StatisticsService {

	List<Object> countUsersPerService();

	List<Object> countUsersPerIdpAndService(ServiceEntity service);

	List<Object> countUsersPerMonth();

	List<Object> countRegistriesPerMonthAndService();

}
