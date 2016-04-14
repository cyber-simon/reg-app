package edu.kit.scc.webreg.approval;

import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ApprovalService;

public class ApproverRoleApprovalWorkflow extends AbstractApprovalWorkflow {

	@Override
	public String getName() {
		return "ApproverRoleApprovalWorkflow";
	}

	@Override
	public void startWorkflow(RegistryEntity registry) throws RegisterException {
		registry.setRegistryStatus(RegistryStatus.PENDING);
		registry.setLastStatusChange(new Date());
	}

	public void denyApproval(RegistryEntity registry, String executor) throws RegisterException {
		try {
			InitialContext ic = new InitialContext();
				
			ApprovalService approvalService = (ApprovalService) ic.lookup("global/bwreg/bwreg-service/ApprovalServiceImpl!edu.kit.scc.webreg.service.reg.ApprovalService");

			approvalService.denyApproval(registry, executor, null);
		} catch (NamingException e) {
			throw new RegisterException(e);
		}		
	}
	
	public void approveRegistry(RegistryEntity registry, String executor) throws RegisterException {
		try {
			InitialContext ic = new InitialContext();
				
			ApprovalService approvalService = (ApprovalService) ic.lookup("global/bwreg/bwreg-service/ApprovalServiceImpl!edu.kit.scc.webreg.service.reg.ApprovalService");

			approvalService.approve(registry, executor, null);
		} catch (NamingException e) {
			throw new RegisterException(e);
		}
	}

}
