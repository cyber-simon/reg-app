package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:38.259+0100")
@StaticMetamodel(UserEntity.class)
public class UserEntity_ {
	public static volatile SingularAttribute<UserEntity, Object> idp;
	public static volatile CollectionAttribute<UserEntity, Object> roles;
	public static volatile CollectionAttribute<UserEntity, Object> groups;
	public static volatile CollectionAttribute<UserEntity, Object> userAttrs;
	public static volatile SingularAttribute<UserEntity, Object> primaryGroup;
}
