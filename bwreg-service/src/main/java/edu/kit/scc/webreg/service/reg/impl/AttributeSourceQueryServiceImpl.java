package edu.kit.scc.webreg.service.reg.impl;

import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.as.AttributeSourceWorkflow;
import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.AuditDetailDao;
import edu.kit.scc.webreg.dao.AuditEntryDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.dao.as.AttributeSourceDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.AttributeSourceQueryService;

@Stateless
public class AttributeSourceQueryServiceImpl implements AttributeSourceQueryService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private AttributeSourceDao attributeSourceDao;
	
	@Inject
	private ASUserAttrDao asUserAttrDao;
	
	@Inject
	private ASUserAttrValueDao asValueDao;
	
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
		user = userDao.findById(user.getId());
		
		ASUserAttrEntity asUserAttr = asUserAttrDao.findASUserAttr(user, attributeSource);
		
		if (asUserAttr == null) {
			asUserAttr = asUserAttrDao.createNew();
			asUserAttr.setAttributeSource(attributeSource);
			asUserAttr.setUser(user);
			asUserAttr = asUserAttrDao.persist(asUserAttr);
		}
		
		for (AttributeSourceServiceEntity asse : attributeSource.getAttributeSourceServices()) {
			logger.debug("Attributes requested for service {}", asse.getService().getName());			
		}
		
		AttributeSourceWorkflow workflow = getWorkflowInstance(attributeSource.getAsClass());
		
		AttributeSourceAuditor auditor = new AttributeSourceAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(attributeSource.getName() + "-AttributeSource-Audit");
		auditor.setDetail("Updateing attributes for user " + user.getEppn());
		auditor.setAsUserAttr(asUserAttr);
		
		workflow.pollUserAttributes(asUserAttr, asValueDao, auditor);
		asUserAttr.setLastSuccessfulQuery(new Date());
		asUserAttr = asUserAttrDao.persist(asUserAttr);
		
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
