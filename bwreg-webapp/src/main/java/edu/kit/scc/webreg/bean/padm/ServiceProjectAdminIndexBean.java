package edu.kit.scc.webreg.bean.padm;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.project.ProjectService;

@Named
@ViewScoped
public class ServiceProjectAdminIndexBean implements Serializable {

	private static final long serialVersionUID = 1L;

    private Long serviceId;

	@Inject
	private ServiceService serviceService;
	
	@Inject
	private ProjectService projectService;

    @Inject
    private AuthorizationBean authBean;

    private ServiceEntity serviceEntity;
	private List<ProjectServiceEntity> projectServiceList;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (! authBean.isUserServiceProjectAdmin(getServiceEntity()))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.fetch(serviceId);
		}
		
		this.serviceId = serviceId;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public void setServiceEntity(ServiceEntity serviceEntity) {
		this.serviceEntity = serviceEntity;
	}

	public List<ProjectServiceEntity> getProjectServiceList() {
		if (projectServiceList == null) {
			projectServiceList = projectService.findAllByService(serviceEntity);
		}
		return projectServiceList;
	}
}
