package edu.kit.scc.webreg.entity.oidc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Entity(name = "ServiceOidcClientEntity")
@Table(name = "service_oidc_client")
public class ServiceOidcClientEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;

	@ManyToOne(targetEntity = OidcClientConfigurationEntity.class)
	private OidcClientConfigurationEntity clientConfig;

	@ManyToOne(targetEntity = ScriptEntity.class)
	private ScriptEntity script;
	
	@Column(name = "wants_elevation")
	private Boolean wantsElevation;
	
	@Column(name = "order_criteria")
	private Integer orderCriteria;
	
	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}
	public ScriptEntity getScript() {
		return script;
	}

	public void setScript(ScriptEntity script) {
		this.script = script;
	}

	public OidcClientConfigurationEntity getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(OidcClientConfigurationEntity clientConfig) {
		this.clientConfig = clientConfig;
	}

	public Boolean getWantsElevation() {
		return wantsElevation;
	}

	public void setWantsElevation(Boolean wantsElevation) {
		this.wantsElevation = wantsElevation;
	}

	public Integer getOrderCriteria() {
		return orderCriteria;
	}

	public void setOrderCriteria(Integer orderCriteria) {
		this.orderCriteria = orderCriteria;
	}
}
