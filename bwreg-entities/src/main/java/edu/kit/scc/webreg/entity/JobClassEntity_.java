package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(JobClassEntity.class)
public abstract class JobClassEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<JobClassEntity, Boolean> singleton;
	public static volatile SingularAttribute<JobClassEntity, String> jobClassName;
	public static volatile MapAttribute<JobClassEntity, String, String> jobStore;
	public static volatile SingularAttribute<JobClassEntity, String> name;

}

