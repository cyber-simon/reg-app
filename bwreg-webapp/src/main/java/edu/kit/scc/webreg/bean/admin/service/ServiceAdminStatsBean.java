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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.chart.PieChartModel;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.StatisticsService;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
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
			
			for (Object o : getUserPerIdpList()) {
				List<Object> ol = (List<Object>) o;
				SamlIdpMetadataEntity i = (SamlIdpMetadataEntity) ol.get(1);
				userPerIdpPie.set(i.getOrgName(), (Number) ol.get(0));
			}

			userPerIdpPie.setTitle(serviceEntity.getName() + " Registrations per IDP");
			userPerIdpPie.setLegendPosition("e");
			userPerIdpPie.setLegendRows(10);
			userPerIdpPie.setLegendCols(1);
			userPerIdpPie.setDiameter(300);
			userPerIdpPie.setShowDataLabels(true);
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
