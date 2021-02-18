package edu.kit.scc.webreg.job;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.LocalGroupService;
import edu.kit.scc.webreg.service.ServiceService;

public class BulkConnectLocalGroups extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(BulkConnectLocalGroups.class);
		
		logger.info("Starting BulkConnectLocalGroups");

		if (! getJobStore().containsKey("ssn_from")) {
			logger.warn("BulkConnectLocalGroups Job is not configured correctly. ssn_from Parameter is missing in JobMap");
			return;
		}

		if (! getJobStore().containsKey("ssn_to")) {
			logger.warn("BulkConnectLocalGroups Job is not configured correctly. ssn_to Parameter is missing in JobMap");
			return;
		}

		String ssnFrom = getJobStore().get("ssn_from");
		String ssnTo = getJobStore().get("ssn_to");
		String filterRegex = getJobStore().getOrDefault("filter_regex", "^.*$");
		
		try {
			InitialContext ic = new InitialContext();
			
			ServiceService serviceService = (ServiceService) ic.lookup("global/bwreg/bwreg-service/ServiceServiceImpl!edu.kit.scc.webreg.service.ServiceService");
			LocalGroupService service = (LocalGroupService) ic.lookup("global/bwreg/bwreg-service/LocalGroupServiceImpl!edu.kit.scc.webreg.service.LocalGroupService");

			ServiceEntity fromService = serviceService.findByShortName(ssnFrom);
			ServiceEntity toService = serviceService.findByShortName(ssnTo);
			
			if (fromService == null || toService == null) {
				logger.warn("BulkConnectLocalGroups Job cannot find service by ssn");
				return;
			}
			
			service.createServiceGroupFlagsBulk(fromService, toService, filterRegex);
			
		} catch (NamingException e) {
			logger.warn("Could not BulkConnectLocalGroups: {}", e);
		}		
	}
}
