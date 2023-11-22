package edu.kit.scc.webreg.bootstrap;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.DroolsCompiler;
import edu.kit.scc.webreg.drools.DroolsCompilerException;
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

	private DroolsCompiler compiler;
	
	private Object lock = new Object();
	
	public void reload() {
		synchronized (lock) {
			
			List<BusinessRulePackageEntity> ruleList = 
					rulePackageService.findAllNewer(nodeConfiguration.getRulesConfigured());
			
			for (BusinessRulePackageEntity rulePackage : ruleList) {
				logger.info("Reloading BPM/ BusinessRules package {}", rulePackage.getKnowledgeBaseName());
				try {
					compiler.compileRule(rulePackage);
				} catch (DroolsCompilerException e) {
					logWarning(rulePackage, e.getMessageList());
				}
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

			compiler = new DroolsCompiler(KieServices.Factory.get());

			List<BusinessRulePackageEntity> ruleList = rulePackageService.findAll();
			
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
				
				try {
					compiler.compileRule(rulePackage);
				} catch (DroolsCompilerException e) {
					logWarning(rulePackage, e.getMessageList());
				}
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
				try {
					compiler.compileRule(permitAllPackage);
				} catch (DroolsCompilerException e) {
					logWarning(permitAllPackage, e.getMessageList());
				}
			}			

			nodeConfiguration.setRulesConfigured(new Date());
		}    	
    }
    
    private void logWarning(BusinessRulePackageEntity rulePackage, List<Message> messageList) {
		logger.warn("Compilation of package {} failed!", rulePackage.getKnowledgeBaseName());
		if (messageList != null) {
			for (Message msg : messageList) {
				logger.warn(msg.getText());
			}
		}
    }
}
