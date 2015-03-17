package edu.kit.scc.webreg.as;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.entity.AuditStatus;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueStringEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public abstract class AbstractAttributeSourceWorkflow implements AttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	protected static final Logger logger = LoggerFactory.getLogger(AbstractAttributeSourceWorkflow.class);

	protected PropertyReader prop;

	public void init(ASUserAttrEntity asUserAttr)
			throws RegisterException {
		prop = new PropertyReader(asUserAttr.getAttributeSource().getAsProps());
	}
	
	protected void createOrUpdateValue(String key, Object o, ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao, AttributeSourceAuditor auditor) {
		if (o == null || key == null) {
			logger.warn("Null keys or values cannot be updated");
			return;
		}
		
		if (o instanceof String) {
			String value = (String) o;
			ASUserAttrValueEntity asValue = asValueDao.findValueByKey(asUserAttr, key);
			
			if (asValue == null) {
				asValue = asValueDao.createNewString();
				asValue.setKey(key);
				asValue.setAsUserAttr(asUserAttr);
				auditor.logAction("as-workflow", "CREATE VALUE (String)", key, "", AuditStatus.SUCCESS);
			}
			else if (! (asValue instanceof ASUserAttrValueStringEntity)) {
				asValueDao.delete(asValue);
				auditor.logAction("as-workflow", "DELETE VALUE", key, "", AuditStatus.SUCCESS);
				asValue = asValueDao.createNewString();
				asValue.setKey(key);
				asValue.setAsUserAttr(asUserAttr);
				auditor.logAction("as-workflow", "CREATE VALUE (String)", key, "", AuditStatus.SUCCESS);
			}
			ASUserAttrValueStringEntity asStringValue = (ASUserAttrValueStringEntity) asValue;
			if (! value.equals(asStringValue.getValueString())) {
				auditor.logAction("as-workflow", "UPDATE VALUE (String)", key, asStringValue.getValueString() + " -> " + value, AuditStatus.SUCCESS);
				asStringValue.setValueString(value);
			}
			
			asValueDao.persist(asValue);
		}
		else {
			logger.warn("Cannot process value of type {}", o.getClass());
		}
	}
}
