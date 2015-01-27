package edu.kit.scc.webreg.drools;

import java.io.StringReader;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.NodeConfiguration;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;
import edu.kit.scc.webreg.service.BusinessRuleService;

@Stateless
public class BpmProcessService {

	private static final String permitAllRule = "global org.slf4j.Logger logger;";
	
	@Inject
	private Logger logger;

	@Inject
	private BusinessRulePackageService rulePackageService;
	
	@Inject
	private BusinessRuleService ruleService;
	
	@Inject
	private NodeConfiguration nodeConfiguration;

	private Object lock = new Object();
	
	public void reload() {
		synchronized (lock) {
			
			List<BusinessRulePackageEntity> ruleList = 
					rulePackageService.findAllNewer(nodeConfiguration.getRulesConfigured());
			
			KieServices ks = KieServices.Factory.get();

			for (BusinessRulePackageEntity rulePackage : ruleList) {
				logger.info("Reloading BPM/ BusinessRules package {}", rulePackage.getKnowledgeBaseName());
				processPackage(rulePackage, ks);
			}
			
			nodeConfiguration.setRulesConfigured(new Date());
		}		
	}
	
    public void init() {

		synchronized (lock) {

	    	/*
			 * Convert old style rules to packages
			 */
			List<BusinessRuleEntity> oldStyleRuleList = ruleService.findAllKnowledgeBaseNotNull();
    	
	    	for (BusinessRuleEntity rule : oldStyleRuleList) {
	
	    		if (rule.getRulePackage() == null) {
		    		logger.info("Converting old rule {} with base {}", rule.getName(), rule.getKnowledgeBaseName());
		    		BusinessRulePackageEntity rulePackage = rulePackageService.findByNameAndVersion(rule.getKnowledgeBaseName(), "1.0.0");
		    		if (rulePackage == null) {
		    			rulePackage = rulePackageService.createNew();
		    			rulePackage.setKnowledgeBaseName(rule.getKnowledgeBaseName());
		    			rulePackage.setKnowledgeBaseVersion("1.0.0");
		    			rulePackage.setRules(new HashSet<BusinessRuleEntity>());
		    			rulePackage = rulePackageService.save(rulePackage);
		    		}
		    		
		    		rule.setRulePackage(rulePackage);
		    		rulePackage.getRules().add(rule);
	    		}
	    		rule.setKnowledgeBaseName(null);
	    	}
	    	
	    	/*
	    	 * Load all rules into KnowledgeBase
	    	 */
	    	
			logger.info("Building BPM/ Business rules");
	
			List<BusinessRulePackageEntity> ruleList = rulePackageService.findAll();
			
			KieServices ks = KieServices.Factory.get();

			// Create permit all package if not in database
			// after processing all rulePackages
			BusinessRulePackageEntity permitAllPackage = null;
			
			for (BusinessRulePackageEntity rulePackage : ruleList) {
				if (rulePackage.getPackageName() != null &&
						rulePackage.getPackageName().equals("default") &&
						rulePackage.getKnowledgeBaseName().equals("permitAllRule") &&
						rulePackage.getKnowledgeBaseVersion().equals("1.0.0")) {
					permitAllPackage = rulePackage;
				}
				
				if (rulePackage.getPackageName() == null) {
					rulePackage.setPackageName("null");
				}
				
				processPackage(rulePackage, ks);
			}
			
			if (permitAllPackage == null) {
				permitAllPackage = rulePackageService.createNew();
				permitAllPackage.setPackageName("default");
				permitAllPackage.setKnowledgeBaseName("permitAllRule");
				permitAllPackage.setKnowledgeBaseVersion("1.0.0");
				permitAllPackage.setRules(new HashSet<BusinessRuleEntity>(1));
				BusinessRuleEntity rule = ruleService.createNew();
				rule.setName("permitAllRule");
				rule.setRuleType("DRL");
				rule.setRule(permitAllRule);
				rule.setRulePackage(permitAllPackage);
				permitAllPackage.getRules().add(rule);				
				processPackage(permitAllPackage, /*repository,*/ ks);
			}			

			nodeConfiguration.setRulesConfigured(new Date());
		}    	
    }
    
    private void processPackage(BusinessRulePackageEntity rulePackage, KieServices ks) {
    	
		logger.info("Processing rulePackage {} ({})", rulePackage.getKnowledgeBaseName(),
				rulePackage.getKnowledgeBaseVersion());

		ReleaseId releaseId = ks.newReleaseId(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
				rulePackage.getKnowledgeBaseVersion());
		
		Resource[] resources = new Resource[rulePackage.getRules().size()];

		int i = 0;
		for (BusinessRuleEntity rule : rulePackage.getRules()) {
			resources[i] = ResourceFactory.newReaderResource(new StringReader(rule.getRule()));
			if (rule.getRuleType().equals("DRL") || rule.getRuleType().equals("BPMN2")) {
				resources[i].setResourceType(ResourceType.getResourceType(rule.getRuleType()));
			}
			resources[i].setSourcePath("kbase/" + rule.getName());
			i++;
		}
		
		KieModule kieModule = ks.getRepository().getKieModule(releaseId);
		
		if (kieModule != null) {
			ks.getRepository().removeKieModule(releaseId);
		}
		
		byte[] jar = createJar(ks, releaseId, resources);
		
		if (jar == null) {
			logger.warn("Compilation of rule failed. Returning");
			return;
		}
		
		byte[] pomBytes = KieBuilderImpl.generatePomXml(releaseId).getBytes();
    }
    
	private byte[] createJar(KieServices ks, ReleaseId releaseId, Resource... resources) {
		KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(releaseId).writeKModuleXML(kmodule);
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] != null) {
				kfs.write(resources[i]);
			}
		}
		KieBuilder kbuilder = ks.newKieBuilder(kfs).buildAll();
		List<Message> messageList = kbuilder.getResults().getMessages();

		for (Message m : messageList) {
			logger.warn(m.getText());
		}
		
		InternalKieModule kieModule = (InternalKieModule) ks.getRepository()
				.getKieModule(releaseId);
		if (kieModule == null)
			return null;
		else {
			byte[] jar = kieModule.getBytes();
			return jar;
		}
	}
	
    private String kmodule = "<kmodule xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "         xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">\n" +
//            "  <kbase name=\"kbase1\" default=\"true\" eventProcessingMode=\"stream\" equalsBehavior=\"identity\" scope=\"javax.enterprise.context.ApplicationScoped\">\n" +
//            "    <ksession name=\"ksession1\" type=\"stateful\" default=\"true\" clockType=\"realtime\" scope=\"javax.enterprise.context.ApplicationScoped\"/>\n" +
//            "  </kbase>\n" +
            "</kmodule>";
	
}
