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
package edu.kit.scc.webreg.bean.project;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.Serializable;
import java.util.List;

import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity_;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ListPublicProjectsIndexBean implements Serializable {

 	private static final long serialVersionUID = 1L;

 	@Inject
 	private SessionManager session;
 	
 	@Inject
 	private ProjectService projectService;
 	
    private List<ProjectEntity> projectList;
        
	public void preRenderView(ComponentSystemEvent ev) {
	
	}

	public List<ProjectEntity> getProjectList() {
		if (projectList == null) {
			projectList = projectService.findAll(unlimited(), ascendingBy(ProjectEntity_.name), 
						equal(ProjectEntity_.published, true));
		}
		return projectList;
	}
}
