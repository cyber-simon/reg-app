package edu.kit.scc.webreg.service.reg.impl;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.as.AttributeSourceUpdater;
import edu.kit.scc.webreg.as.AttributeSourceWorkflow;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.reg.AttributeSourceQueryService;

@Stateless
public class AttributeSourceQueryServiceImpl implements AttributeSourceQueryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceUpdater updater;

	@Override
	public Boolean updateUserAttributes(UserEntity user, AttributeSourceEntity attributeSource, String executor) 
		throws UserUpdateException {
		
		return updater.updateUserAttributes(user, attributeSource, executor);
	}
	
	public AttributeSourceWorkflow getWorkflowInstance(String className) {
		return updater.getWorkflowInstance(className);
	}
	
}
