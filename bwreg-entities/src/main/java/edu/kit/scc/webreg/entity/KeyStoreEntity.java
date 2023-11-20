package edu.kit.scc.webreg.entity;

import java.sql.Types;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "key_store")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class KeyStoreEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)
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
