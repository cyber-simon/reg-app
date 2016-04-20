package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ApproverRoleEntity.class)
public abstract class ApproverRoleEntity_ extends edu.kit.scc.webreg.entity.RoleEntity_ {

	public static volatile SingularAttribute<ApproverRoleEntity, String> approvalBean;
	public static volatile SetAttribute<ApproverRoleEntity, ServiceEntity> approverForServices;

}

