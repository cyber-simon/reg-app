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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNotNull;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.BusinessRuleDao;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;
import edu.kit.scc.webreg.entity.BusinessRuleEntity_;
import edu.kit.scc.webreg.service.BusinessRuleService;

@Stateless
public class BusinessRuleServiceImpl extends BaseServiceImpl<BusinessRuleEntity> implements BusinessRuleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private BusinessRuleDao dao;

	@Override
	public List<BusinessRuleEntity> findAllKnowledgeBaseNotNull() {
		return dao.findAll(isNotNull(BusinessRuleEntity_.knowledgeBaseName));
	}

	@Override
	protected BaseDao<BusinessRuleEntity> getDao() {
		return dao;
	}

	@Override
	public void replaceRegexSingle(Long ruleId, String regex, String replace) {
		BusinessRuleEntity rule = dao.fetch(ruleId);
		if (rule != null) {
			replaceRegexIntern(rule, regex, replace);
		}
	}

	@Override
	public void replaceRegex(String regex, String replace) {
		List<BusinessRuleEntity> ruleList = dao.findAll();
		for (BusinessRuleEntity rule : ruleList) {
			replaceRegexIntern(rule, regex, replace);
		}
	}

	private void replaceRegexIntern(BusinessRuleEntity rule, String regex, String replace) {
		logger.info("Processing rule {}", rule.getName());
		String newRule = rule.getRule().replaceAll(regex, replace);
		rule.setRule(newRule);
		;
	}
}
