package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:38.197+0100")
@StaticMetamodel(JobScheduleEntity.class)
public class JobScheduleEntity_ extends AbstractBaseEntity_ {
	public static volatile SingularAttribute<JobScheduleEntity, String> name;
	public static volatile SingularAttribute<JobScheduleEntity, JobClassEntity> jobClass;
	public static volatile SingularAttribute<JobScheduleEntity, String> second;
	public static volatile SingularAttribute<JobScheduleEntity, String> minute;
	public static volatile SingularAttribute<JobScheduleEntity, String> hour;
	public static volatile SingularAttribute<JobScheduleEntity, String> month;
	public static volatile SingularAttribute<JobScheduleEntity, String> year;
	public static volatile SingularAttribute<JobScheduleEntity, String> dayOfWeek;
	public static volatile SingularAttribute<JobScheduleEntity, String> dayOfMonth;
	public static volatile SingularAttribute<JobScheduleEntity, Boolean> disabled;
}
