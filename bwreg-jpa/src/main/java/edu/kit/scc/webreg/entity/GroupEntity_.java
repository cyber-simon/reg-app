package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(GroupEntity.class)
public abstract class GroupEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SetAttribute<GroupEntity, RoleEntity> adminRoles;
	public static volatile SetAttribute<GroupEntity, GroupEntity> children;
	public static volatile SetAttribute<GroupEntity, RoleGroupEntity> roles;
	public static volatile SingularAttribute<GroupEntity, String> name;
	public static volatile SingularAttribute<GroupEntity, Integer> gidNumber;
	public static volatile SingularAttribute<GroupEntity, GroupStatus> groupStatus;
	public static volatile SetAttribute<GroupEntity, UserGroupEntity> users;
	public static volatile SetAttribute<GroupEntity, GroupEntity> parents;

}

