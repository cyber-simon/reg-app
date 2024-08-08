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
package edu.kit.scc.webreg.job;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThanOrEqualTo;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.notEqual;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.or;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity_;
import edu.kit.scc.webreg.entity.project.ProjectStatus;
import edu.kit.scc.webreg.service.project.ProjectService;

public class ProjectsSyncToGroup extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ProjectsSyncToGroup.class);

		Long olderThanMillis;

		if (!getJobStore().containsKey("older_than_millis")) {
			olderThanMillis = 24L * 60L * 60L * 1000L;
		} else {
			olderThanMillis = Long.parseLong(getJobStore().get("older_than_millis"));
		}

		Date olderThan = Date.from(Instant.now().minus(olderThanMillis, MILLIS));

		Integer limit;

		if (!getJobStore().containsKey("limit")) {
			limit = 2;
		} else {
			limit = Integer.parseInt(getJobStore().get("limit"));
		}

		try {
			InitialContext ic = new InitialContext();

			ProjectService projectService = (ProjectService) ic.lookup(
					"global/bwreg/bwreg-service/ProjectServiceImpl!edu.kit.scc.webreg.service.project.ProjectService");

			logger.info("Starting project member sync to group");

			List<ProjectEntity> projectList = projectService.findAll(withLimit(limit),
					ascendingBy(ProjectEntity_.lastSyncToGroup),
					and(or(lessThanOrEqualTo(ProjectEntity_.lastSyncToGroup, olderThan),
							isNull(ProjectEntity_.lastSyncToGroup)),
							or(notEqual(ProjectEntity_.projectStatus, ProjectStatus.DELETED),
									RqlExpressions.isNull(ProjectEntity_.projectStatus))));

			for (ProjectEntity project : projectList) {
				logger.info("Syncing project {}", project.getShortName());
				projectService.syncAllMembersToGroup(project, "project-sync-job");
			}

			logger.info("Project member sync to group done");

		} catch (NamingException e) {
			logger.warn("Could not clear Audit Logs: {}", e);
		}
	}
}
