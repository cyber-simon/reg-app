package edu.kit.scc.webreg.entity.attribute;

import edu.kit.scc.webreg.entity.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity(name = "UserAttributeSetEntity")
public class UserAttributeSetEntity extends AttributeSetEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = UserEntity.class)
	private UserEntity user;

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

}
