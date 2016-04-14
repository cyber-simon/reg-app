package edu.kit.scc.webreg.entity.audit;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import edu.kit.scc.webreg.entity.RegistryEntity;

@StaticMetamodel(AuditApprovalEntity.class)
public class AuditApprovalEntity_ extends AuditEntryEntity_ {
	public static volatile SingularAttribute<AuditApprovalEntity, RegistryEntity> registry;
}
