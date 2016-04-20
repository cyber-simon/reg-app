package edu.kit.scc.webreg.entity.audit;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AuditDetailEntity.class)
public abstract class AuditDetailEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<AuditDetailEntity, String> log;
	public static volatile SingularAttribute<AuditDetailEntity, String> subject;
	public static volatile SingularAttribute<AuditDetailEntity, AuditEntryEntity> auditEntry;
	public static volatile SingularAttribute<AuditDetailEntity, AuditStatus> auditStatus;
	public static volatile SingularAttribute<AuditDetailEntity, String> action;
	public static volatile SingularAttribute<AuditDetailEntity, Date> endTime;
	public static volatile SingularAttribute<AuditDetailEntity, String> object;

}

