package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:38.210+0100")
@StaticMetamodel(RoleEntity.class)
public class RoleEntity_ extends AbstractBaseEntity_ {
	public static volatile SingularAttribute<RoleEntity, String> name;
	public static volatile SetAttribute<RoleEntity, UserRoleEntity> users;
}
