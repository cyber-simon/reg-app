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
package edu.kit.scc.webreg.bean.admin.timer;

import java.io.Serializable;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.bootstrap.NodeConfiguration;
import edu.kit.scc.webreg.entity.ClusterMemberEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.ClusterMemberService;
import edu.kit.scc.webreg.service.timer.StandardScheduler;

@Named
@ViewScoped
public class SchedulerStatusBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private StandardScheduler standardScheduler;
	
	@Inject
	private NodeConfiguration nodeConfiguration;
	
	@Inject
	private ClusterMemberService clusterMemberService;
	
	private LazyDataModel<ClusterMemberEntity> list;
	
    public void preRenderView() {
		if (list == null) {
			list = new GenericLazyDataModelImpl<ClusterMemberEntity, ClusterMemberService>(clusterMemberService);
		}
	}
	
	public NodeConfiguration getNodeConfiguration() {
		return nodeConfiguration;
	}

	public StandardScheduler getStandardScheduler() {
		return standardScheduler;
	}

	public LazyDataModel<ClusterMemberEntity> getList() {
		return list;
	}

	public void setList(LazyDataModel<ClusterMemberEntity> list) {
		this.list = list;
	}

}
