package edu.kit.scc.webreg.hook;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserServiceHook;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;

public class UidNumberFromAssertionUserHook implements UserServiceHook {

	private Logger logger = LoggerFactory.getLogger(UidNumberFromAssertionUserHook.class);
	
	private AttributeMapHelper attrHelper = new AttributeMapHelper();

	private ApplicationConfig appConfig;
	
	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}
		
	@Override
	public void preUpdateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor) throws UserUpdateException {

		if (isResponsibleIntern(user)) {
			logger.info("UidNumberFromAssertion-User Detected. Taking UID from assertion");
			if (attributeMap.get("urn:oid:1.3.6.1.1.1.1.0") != null) {
				Integer newUidNumber = Integer.parseInt(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:1.3.6.1.1.1.1.0"));
				if (user.getUidNumber() == null) {
					user.setUidNumber(newUidNumber);
					auditor.logAction(user.getEppn(), "SET FIELD (UFA)", "uidNumber", "" + user.getUidNumber(), AuditStatus.SUCCESS);
				}
				else if  (! user.getUidNumber().equals(newUidNumber)) {
					auditor.logAction(user.getEppn(), "UPDATE FIELD (UFA)", "uidNumber", "" + user.getUidNumber() +
							" -> " + newUidNumber, AuditStatus.SUCCESS);
					user.setUidNumber(newUidNumber);
				}
			}
			else {
				if (user.getUidNumber() == null) {
					throw new UserUpdateException("UID Number Attribut ist nicht gesetzt und User hat keine");
				}
				// else: Es kommt keine uidNumber (User ist geloescht), aber user hat schon eine
			}
		}		
	
	}

	@Override
	public void postUpdateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor) throws UserUpdateException {

	}
	
	@Override
	public boolean isResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		return isResponsibleIntern(user);
	}
	
	protected boolean isResponsibleIntern(UserEntity user) {
		if (appConfig != null) {
			String entityIdsConfig = "";
			if (appConfig.getConfigValue("UidNumberFromAssertionUserHook_entityIds") != null) {
				entityIdsConfig = appConfig.getConfigValue("UidNumberFromAssertionUserHook_entityIds");
			}
			String[] entityIds = entityIdsConfig.split(" ");
			for (String entityId : entityIds) {
				if (user.getIdp().getEntityId().equals(entityId.trim()))
					return true;
			}
		}
		
		return false;		
	}
	
	@Override
	public boolean isCompleteOverride() {
		return false;
	}
}
