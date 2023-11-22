/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.PolicyDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.PolicyEntity_;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectPolicyType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.PolicyService;

@Stateless
public class PolicyServiceImpl extends BaseServiceImpl<PolicyEntity> implements PolicyService {

	private static final long serialVersionUID = 1;

	@Inject
	private Logger logger;

	@Inject
	private PolicyDao dao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ProjectDao projectDao;
	
	@Inject
	private ScriptingEnv scriptingEnv;

	@Override
	public Map<ProjectServiceEntity, List<PolicyEntity>> findPolicyMapForProject(ProjectEntity project,
			ProjectPolicyType policyType) {

		project = projectDao.fetch(project.getId());
		
		Map<ProjectServiceEntity, List<PolicyEntity>> returnMap = new HashMap<>();
		project.getProjectServices().stream().forEach(projectService -> {
			returnMap.put(projectService,
					dao.findAll(and(equal(PolicyEntity_.projectPolicy, projectService.getService()),
							equal(PolicyEntity_.projectPolicyType, policyType))));
		});

		return returnMap;
	}

	@Override
	public List<PolicyEntity> resolvePoliciesForService(ServiceEntity service, UserEntity user) {
		List<PolicyEntity> policyList = new ArrayList<PolicyEntity>();

		service = serviceDao.fetch(service.getId());
		user = userDao.fetch(user.getId());

		List<PolicyEntity> allPolicyList = new ArrayList<>(service.getPolicies());
		for (PolicyEntity p : allPolicyList) {
			if (p.getHidden() == null || p.getHidden().equals(Boolean.FALSE)) {
				policyList.add(p);
			}
		}

		if (service.getServiceProps().containsKey("override_policy_script")) {
			String scriptName = service.getServiceProps().get("override_policy_script");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);

			if (scriptEntity == null) {
				logger.warn("override_policy_script {} is defined for service {}, but not in database", scriptName,
						service.getName());
			} else {
				if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
					ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

					if (engine == null) {
						logger.warn("override_policy_script {} for service {}, cannot load script enginge: {}",
								scriptName, service.getName(), scriptEntity.getScriptEngine());
					} else {
						try {
							engine.eval(scriptEntity.getScript());

							Invocable invocable = (Invocable) engine;

							invocable.invokeFunction("resolvePolicies", scriptingEnv, allPolicyList, policyList,
									service, user, logger);
						} catch (ScriptException | NoSuchMethodException e) {
							logger.warn("override_policy_script {} for service {} threw error: {}", scriptName,
									service.getName(), e.getMessage());
						}
					}
				}
			}
		}

		Collections.sort(policyList, new Comparator<PolicyEntity>() {
			@Override
			public int compare(PolicyEntity p1, PolicyEntity p2) {
				return p1.getId().compareTo(p2.getId());
			}
		});

		return policyList;
	}

	@Override
	public PolicyEntity findWithAgreemets(Long id) {
		return dao.find(equal(PolicyEntity_.id, id), PolicyEntity_.agreementTexts);
	}

	@Override
	protected BaseDao<PolicyEntity> getDao() {
		return dao;
	}
}
