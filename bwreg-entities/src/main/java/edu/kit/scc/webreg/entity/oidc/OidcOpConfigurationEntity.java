package edu.kit.scc.webreg.entity.oidc;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "OidcOpConfigurationEntity")
@Table(name = "oidc_op_configuration")
public class OidcOpConfigurationEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 64)
	private String name;

	@Column(name = "realm", length = 64)
	private String realm;

	@Column(name = "host", length = 256)
	private String host;

	@Column(name = "private_key")
	@Lob
	@Type(type = "org.hibernate.type.TextType")		
	private String privateKey;
	
	@Column(name = "certificate")
	@Lob
	@Type(type = "org.hibernate.type.TextType")		
	private String certificate;
	
	@Column(name = "standby_private_key")
	@Lob
	@Type(type = "org.hibernate.type.TextType")		
	private String standbyPrivateKey;
	
	@Column(name = "standby_certificate")
	@Lob
	@Type(type = "org.hibernate.type.TextType")		
	private String standbyCertificate;
	
	@Enumerated(EnumType.STRING)
	private OidcOpConfigurationStatusType opStatus; 
	
	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public String getStandbyPrivateKey() {
		return standbyPrivateKey;
	}

	public void setStandbyPrivateKey(String standbyPrivateKey) {
		this.standbyPrivateKey = standbyPrivateKey;
	}

	public String getStandbyCertificate() {
		return standbyCertificate;
	}

	public void setStandbyCertificate(String standbyCertificate) {
		this.standbyCertificate = standbyCertificate;
	}

	public OidcOpConfigurationStatusType getOpStatus() {
		return opStatus;
	}

	public void setOpStatus(OidcOpConfigurationStatusType opStatus) {
		this.opStatus = opStatus;
	}
	
}
