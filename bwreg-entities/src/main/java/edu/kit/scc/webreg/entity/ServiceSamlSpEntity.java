package edu.kit.scc.webreg.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "ServiceSamlSpEntity")
@Table(name = "service_saml_sp")
public class ServiceSamlSpEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;

	@ManyToOne(targetEntity = SamlSpMetadataEntity.class)
	private SamlSpMetadataEntity sp;

	@ManyToOne(targetEntity = SamlIdpConfigurationEntity.class)
	private SamlIdpConfigurationEntity idp;
	
	@ManyToOne (targetEntity = ScriptEntity.class)
	private ScriptEntity script;
	
	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public SamlSpMetadataEntity getSp() {
		return sp;
	}

	public void setSp(SamlSpMetadataEntity sp) {
		this.sp = sp;
	}

	public ScriptEntity getScript() {
		return script;
	}

	public void setScript(ScriptEntity script) {
		this.script = script;
	}

	public SamlIdpConfigurationEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpConfigurationEntity idp) {
		this.idp = idp;
	}
}
