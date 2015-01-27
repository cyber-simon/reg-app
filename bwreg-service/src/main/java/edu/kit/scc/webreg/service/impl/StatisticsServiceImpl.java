package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.StatisticsDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.StatisticsService;

@Stateless
public class StatisticsServiceImpl implements Serializable, StatisticsService {

	private static final long serialVersionUID = 1L;

	@Inject
	private StatisticsDao dao;
	
	@Override
	public List<Object> countUsersPerIdp() {
		return dao.countUsersPerIdp();
	}

	@Override
	public List<Object> countUsersPerIdpAndService(ServiceEntity service) {
		return dao.countUsersPerIdpAndService(service);
	}

	@Override
	public List<Object> countUsersPerMonth() {
		return dao.countUsersPerMonth();
	}	

	@Override
	public List<Object> countRegistriesPerMonthAndService() {
		return dao.countRegistriesPerMonthAndService();
	}
	
	@Override
	public List<Object> countUsersPerService() {
		return dao.countUsersPerService();
	}	
}
