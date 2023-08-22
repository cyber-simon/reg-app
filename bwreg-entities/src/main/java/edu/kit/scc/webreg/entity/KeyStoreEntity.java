package edu.kit.scc.webreg.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "key_store")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class KeyStoreEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String base64EncodedKeyStoreBlob;

	@Column(unique = true)
	private String context;

	public String getBase64EncodedKeyStoreBlob() {
		return base64EncodedKeyStoreBlob;
	}

	public void setBase64EncodedKeyStoreBlob(String keyStoreBlob) {
		base64EncodedKeyStoreBlob = keyStoreBlob;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

}
