package edu.kit.scc.webreg.service.reg.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.as.AttributeSourceWorkflow;
import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
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
	
	@Inject
	private AuditEntryDao auditDao;
	
	@Inject
	private AuditDetailDao auditDetailDao;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public void updateUserAttributes(UserEntity user, AttributeSourceEntity attributeSource, String executor) 
		throws RegisterException {
		
		attributeSource = attributeSourceDao.findById(attributeSource.getId());
		
		AttributeSourceWorkflow workflow = getWorkflowInstance(attributeSource.getAsClass());
		
		AttributeSourceAuditor auditor = new AttributeSourceAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(attributeSource.getName() + "-AttributeSource-Audit");
		auditor.setDetail("Updateing attributes for user " + user.getEppn());
		auditor.setAttributeSource(attributeSource);
		
		workflow.pollUserAttributes(attributeSource, user, auditor);
		
		auditor.finishAuditTrail();		
	}
	
	public AttributeSourceWorkflow getWorkflowInstance(String className) {
		try {
			Object o = Class.forName(className).newInstance();
			if (o instanceof AttributeSourceWorkflow)
				return (AttributeSourceWorkflow) o;
			else {
				logger.warn("AttributeSourceWorkflow bean misconfigured, Object not Type AttributeSourceWorkflow but: {}", o.getClass());
				return null;
			}
		} catch (InstantiationException e) {
			logger.warn("AttributeSourceWorkflow bean misconfigured: {}", e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			logger.warn("AttributeSourceWorkflow bean misconfigured: {}", e.getMessage());
			return null;
		} catch (ClassNotFoundException e) {
			logger.warn("AttributeSourceWorkflow bean misconfigured: {}", e.getMessage());
			return null;
		}
	}
	
}
