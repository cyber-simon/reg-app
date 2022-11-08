package edu.kit.scc.webreg.drools;

import java.io.StringReader;
import java.util.List;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieBuilderImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;

import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;

public class DroolsCompiler {

	private KieServices kieServices;
	
	public DroolsCompiler(KieServices kieServices) {
		super();
		this.kieServices = kieServices;
	}

	public void compileRule(BusinessRulePackageEntity pkg) 
			throws DroolsCompilerException {
		
		ReleaseId releaseId = kieServices.newReleaseId(pkg.getPackageName(), pkg.getKnowledgeBaseName(), 
				pkg.getKnowledgeBaseVersion());

		Resource[] resources = new Resource[pkg.getRules().size()];

		int i = 0;
		for (BusinessRuleEntity rule : pkg.getRules()) {
			if (rule.getRule() != null) {
				resources[i] = ResourceFactory.newReaderResource(new StringReader(rule.getRule()));
				if (rule.getRuleType().equals("DRL") || rule.getRuleType().equals("BPMN2")) {
					resources[i].setResourceType(ResourceType.getResourceType(rule.getRuleType()));
				}
				resources[i].setSourcePath("kbase/" + rule.getName());
				i++;
			}
		}

		if (kieServices.getRepository().getKieModule(releaseId) != null) {
			kieServices.getRepository().removeKieModule(releaseId);
		}

		KieFileSystem kieFileSystem = kieServices.newKieFileSystem().generateAndWritePomXML(releaseId).writeKModuleXML(kmodule);
		for (i = 0; i < resources.length; i++) {
			if (resources[i] != null) {
				kieFileSystem.write(resources[i]);
			}
		}

		KieBuilder kbuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
		List<Message> messageList = kbuilder.getResults().getMessages();

		InternalKieModule kieModule = (InternalKieModule) kieServices.getRepository()
				.getKieModule(releaseId);
		if (kieModule == null) {
			throw new DroolsCompilerException("Something went wrong while compiling rule " + releaseId, messageList);
		}
		else {
			kieModule.getBytes();
			KieBuilderImpl.generatePomXml(releaseId).getBytes();
		}		
	}
	
    private String kmodule = "<kmodule xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "         xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">\n" +
            "</kmodule>";	
}
