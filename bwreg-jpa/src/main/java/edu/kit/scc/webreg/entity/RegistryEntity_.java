package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(RegistryEntity.class)
public abstract class RegistryEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<RegistryEntity, String> approvalBean;
	public static volatile MapAttribute<RegistryEntity, String, String> registryValues;
	public static volatile SetAttribute<RegistryEntity, AgreementTextEntity> agreedTexts;
	public static volatile SingularAttribute<RegistryEntity, Date> lastReconcile;
	public static volatile SingularAttribute<RegistryEntity, Date> lastFullReconcile;
	public static volatile SingularAttribute<RegistryEntity, ServiceEntity> service;
	public static volatile SingularAttribute<RegistryEntity, Date> lastStatusChange;
	public static volatile SingularAttribute<RegistryEntity, RegistryStatus> registryStatus;
	public static volatile SingularAttribute<RegistryEntity, Date> lastAccessCheck;
	public static volatile SingularAttribute<RegistryEntity, UserEntity> user;
	public static volatile SingularAttribute<RegistryEntity, String> registerBean;
	public static volatile SingularAttribute<RegistryEntity, Date> agreedTime;

}

