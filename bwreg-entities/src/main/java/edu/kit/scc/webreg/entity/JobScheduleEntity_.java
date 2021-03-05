package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(JobScheduleEntity.class)
public abstract class JobScheduleEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<JobScheduleEntity, String> dayOfWeek;
	public static volatile SingularAttribute<JobScheduleEntity, String> hour;
	public static volatile SingularAttribute<JobScheduleEntity, String> month;
	public static volatile SingularAttribute<JobScheduleEntity, String> year;
	public static volatile SingularAttribute<JobScheduleEntity, String> dayOfMonth;
	public static volatile SingularAttribute<JobScheduleEntity, JobClassEntity> jobClass;
	public static volatile SingularAttribute<JobScheduleEntity, String> name;
	public static volatile SingularAttribute<JobScheduleEntity, Boolean> disabled;
	public static volatile SingularAttribute<JobScheduleEntity, String> second;
	public static volatile SingularAttribute<JobScheduleEntity, String> minute;

}

