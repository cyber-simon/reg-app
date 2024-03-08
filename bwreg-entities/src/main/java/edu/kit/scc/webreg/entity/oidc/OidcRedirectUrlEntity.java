package edu.kit.scc.webreg.entity.oidc;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "OidcRedirectUrlEntity")
@Table(name = "oidc_client_redirect_url")
public class OidcRedirectUrlEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "redirect_url", length = 2048)
	private String url;

	@ManyToOne(targetEntity = OidcClientConsumerEntity.class)
	private OidcClientConsumerEntity client;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public OidcClientConsumerEntity getClient() {
		return client;
	}

	public void setClient(OidcClientConsumerEntity client) {
		this.client = client;
	}
}
