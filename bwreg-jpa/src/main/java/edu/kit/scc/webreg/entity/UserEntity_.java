package edu.kit.scc.webreg.entity;

import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserEntity.class)
public abstract class UserEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SetAttribute<UserEntity, ASUserAttrEntity> userAttrs;
	public static volatile SingularAttribute<UserEntity, UserStatus> userStatus;
	public static volatile SingularAttribute<UserEntity, String> persistentSpId;
	public static volatile SingularAttribute<UserEntity, String> surName;
	public static volatile MapAttribute<UserEntity, String, String> genericStore;
	public static volatile SingularAttribute<UserEntity, String> persistentIdpId;
	public static volatile SingularAttribute<UserEntity, String> eppn;
	public static volatile SingularAttribute<UserEntity, String> givenName;
	public static volatile SetAttribute<UserEntity, UserRoleEntity> roles;
	public static volatile SetAttribute<UserEntity, UserGroupEntity> groups;
	public static volatile MapAttribute<UserEntity, String, String> attributeStore;
	public static volatile SingularAttribute<UserEntity, String> locale;
	public static volatile SingularAttribute<UserEntity, Date> lastFailedUpdate;
	public static volatile SingularAttribute<UserEntity, String> persistentId;
	public static volatile SetAttribute<UserEntity, String> emailAddresses;
	public static volatile SingularAttribute<UserEntity, SamlIdpMetadataEntity> idp;
	public static volatile SingularAttribute<UserEntity, Integer> uidNumber;
	public static volatile SingularAttribute<UserEntity, Date> lastUpdate;
	public static volatile SingularAttribute<UserEntity, Date> lastStatusChange;
	public static volatile SingularAttribute<UserEntity, String> theme;
	public static volatile SingularAttribute<UserEntity, String> email;
	public static volatile SingularAttribute<UserEntity, GroupEntity> primaryGroup;

}

