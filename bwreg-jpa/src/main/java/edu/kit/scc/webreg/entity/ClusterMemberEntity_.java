package edu.kit.scc.webreg.entity;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ClusterMemberEntity.class)
public abstract class ClusterMemberEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<ApplicationConfigEntity, String> nodeName;
	public static volatile SingularAttribute<ClusterMemberEntity, ClusterMemberStatus> clusterMemberStatus;
	public static volatile SingularAttribute<UserEntity, Date> lastStatusChange;
	public static volatile SingularAttribute<ClusterMemberEntity, ClusterSchedulerStatus> clusterSchedulerStatus;
	public static volatile SingularAttribute<UserEntity, Date> lastSchedulerStatusChange;
}

