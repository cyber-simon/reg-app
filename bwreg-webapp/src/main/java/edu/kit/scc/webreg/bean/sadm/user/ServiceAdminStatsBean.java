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
package edu.kit.scc.webreg.bean.sadm.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.StatisticsService;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class ServiceAdminStatsBean implements Serializable {

 	private static final long serialVersionUID = 1L;

	@Inject
    private ServiceService serviceService;
    
	@Inject
	private StatisticsService service;
	
    @Inject
    private AuthorizationBean authBean;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
    private ServiceEntity serviceEntity;
    
    private Long serviceId;

    private List<Object> userPerIdpList;
	private PieChartModel userPerIdpPie;
   
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.findById(serviceId);
		}

		if (! authBean.isUserServiceAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public List<Object> getUserPerIdpList() {
		if (userPerIdpList == null) {
			userPerIdpList = service.countUsersPerIdpAndService(serviceEntity);
		}
		return userPerIdpList;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public PieChartModel getUserPerIdpPie() {
		if (userPerIdpPie == null) {
			userPerIdpPie = new PieChartModel();
			ChartData data = new ChartData();
			PieChartDataSet dataSet = new PieChartDataSet();
	        List<Number> values = new ArrayList<>();
	        List<String> labels = new ArrayList<>();
	        
			for (Object o : getUserPerIdpList()) {
				List<Object> ol = (List<Object>) o;
				SamlIdpMetadataEntity i = (SamlIdpMetadataEntity) ol.get(1);
				
				values.add((Number) ol.get(0));
				labels.add(i.getOrgName());
			}

	        List<String> bgColor = new ArrayList<>();
	        bgColor.add("#00876c");
			bgColor.add("#439981");
			bgColor.add("#6aaa96");
			bgColor.add("#8cbcac");
			bgColor.add("#aecdc2");
			bgColor.add("#cfdfd9");
			bgColor.add("#f1f1f1");
			bgColor.add("#f1d4d4");
			bgColor.add("#f0b8b8");
			bgColor.add("#ec9c9d");
			bgColor.add("#e67f83");
			bgColor.add("#de6069");
			bgColor.add("#d43d51");
	        dataSet.setBackgroundColor(bgColor);
	        
			dataSet.setData(values);
			data.addChartDataSet(dataSet);
			data.setLabels(labels);
			userPerIdpPie.setData(data);
		}
		return userPerIdpPie;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public void setServiceEntity(ServiceEntity serviceEntity) {
		this.serviceEntity = serviceEntity;
	}

}
