package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:38.175+0100")
@StaticMetamodel(GroupEntity.class)
public class GroupEntity_ extends AbstractBaseEntity_ {
	public static volatile SingularAttribute<GroupEntity, Integer> gidNumber;
	public static volatile SingularAttribute<GroupEntity, String> name;
	public static volatile SingularAttribute<GroupEntity, GroupStatus> groupStatus;
	public static volatile SetAttribute<GroupEntity, UserGroupEntity> users;
	public static volatile SetAttribute<GroupEntity, RoleGroupEntity> roles;
	public static volatile SetAttribute<GroupEntity, RoleEntity> adminRoles;
	public static volatile SetAttribute<GroupEntity, GroupEntity> parents;
	public static volatile SetAttribute<GroupEntity, GroupEntity> children;
}
