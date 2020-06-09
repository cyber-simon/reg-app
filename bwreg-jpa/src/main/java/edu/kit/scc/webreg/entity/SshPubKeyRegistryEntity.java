package edu.kit.scc.webreg.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "SshPubKeyRegistryEntity")
@Table(name = "ssh_pub_key_registry")
public class SshPubKeyRegistryEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@ManyToOne(targetEntity = RegistryEntity.class)
	private RegistryEntity registry;
	
	@ManyToOne(targetEntity = SshPubKeyEntity.class)
	private SshPubKeyEntity sshPubKey;

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}

	public SshPubKeyEntity getSshPubKey() {
		return sshPubKey;
	}

	public void setSshPubKey(SshPubKeyEntity sshPubKey) {
		this.sshPubKey = sshPubKey;
	}
	
}
