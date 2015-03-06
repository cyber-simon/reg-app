package edu.kit.scc.webreg.service.reg.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.AttributeSourceService;

@Stateless
public class AttributeSourceServiceImpl implements AttributeSourceService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private AttributeSourceDao attributeSourceDao;
	
	public void updateUserAttributes(UserEntity user, AttributeSourceEntity attributeSource) 
		throws RegisterException {
		
		
		
	}
}
