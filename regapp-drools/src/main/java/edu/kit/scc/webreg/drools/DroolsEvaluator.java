package edu.kit.scc.webreg.drools;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

public class DroolsEvaluator {

	private KieServices kieServices;
	
	public DroolsEvaluator(KieServices kieServices) {
		super();
		this.kieServices = kieServices;
	}

	public KieSession getStatefulSession(String packageName, String knowledgeBaseName, String knowledgeBaseVersion) {
		
		ReleaseId releaseId = kieServices.newReleaseId(packageName, knowledgeBaseName, knowledgeBaseVersion);
		KieContainer kc = kieServices.newKieContainer(releaseId);
		return kc.newKieSession();
	}

	public KieSession getStatefulSession(String unitId) {
		String[] splits = unitId.split(":");
		
		if (splits.length != 3)
			throw new IllegalArgumentException("unitId must contain two :");
		
		return getStatefulSession(splits[0], splits[1], splits[2]);
	}
	
	public List<ServiceEntity> checkServiceFilterRule(String unitId, IdentityEntity identity, List<ServiceEntity> serviceList,
			Set<GroupEntity> groups, Set<RoleEntity> roles, Logger logger) 
			throws DroolsConfigurationException {
		
		KieSession ksession = getStatefulSession(unitId);

		if (ksession == null)
			throw new DroolsConfigurationException("Es ist keine valide Regel fuer den Benutzerzugriff konfiguriert");

		ksession.setGlobal("logger", logger);
		ksession.insert(identity);
		for (UserEntity user : identity.getUsers())
			ksession.insert(user);
		for (GroupEntity group : groups)
			ksession.insert(group);
		for (ServiceEntity service : serviceList)
			ksession.insert(service);
		ksession.insert(new Date());
		
		ksession.fireAllRules();

		List<Object> objectList = new ArrayList<Object>(ksession.getObjects());
		List<ServiceEntity> removeList = new ArrayList<ServiceEntity>();
		
		for (Object o : objectList) {
			FactHandle factHandle = ksession.getFactHandle(o);
			if (factHandle != null)
				ksession.delete(factHandle);
			
			if (o instanceof ServiceEntity) {
				removeList.add((ServiceEntity) o);
			}
		}

		ksession.dispose();

		List<ServiceEntity> returnList = new ArrayList<ServiceEntity>(serviceList);
		returnList.removeAll(removeList);
		
		return returnList;
	}	
	
}
