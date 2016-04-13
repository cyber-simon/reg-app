package edu.kit.scc.webreg.entity.audit;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import edu.kit.scc.webreg.entity.RegistryEntity;

@StaticMetamodel(AuditRegistryEntity.class)
public class AuditRegistryEntity_ extends AuditEntryEntity_ {
	public static volatile SingularAttribute<AuditRegistryEntity, RegistryEntity> registry;
}
