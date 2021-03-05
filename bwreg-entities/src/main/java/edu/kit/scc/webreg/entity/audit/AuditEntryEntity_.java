package edu.kit.scc.webreg.entity.audit;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AuditEntryEntity.class)
public abstract class AuditEntryEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<AuditEntryEntity, String> executor;
	public static volatile SetAttribute<AuditEntryEntity, AuditDetailEntity> auditDetails;
	public static volatile SingularAttribute<AuditEntryEntity, String> name;
	public static volatile SingularAttribute<AuditEntryEntity, AuditEntryEntity> parentEntry;
	public static volatile SingularAttribute<AuditEntryEntity, Date> startTime;
	public static volatile SingularAttribute<AuditEntryEntity, Date> endTime;
	public static volatile SingularAttribute<AuditEntryEntity, String> detail;
	public static volatile SetAttribute<AuditEntryEntity, AuditEntryEntity> childEntries;

}

