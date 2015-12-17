package edu.kit.scc.webreg.entity;

import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(ServiceEntity.class)
public class ServiceEntity_ {
	public static volatile SingularAttribute<ServiceEntity, Boolean> hidden;

	public static volatile SingularAttribute<ServiceEntity, Object> adminRole;
	public static volatile SingularAttribute<ServiceEntity, Object> hotlineRole;
	public static volatile SingularAttribute<ServiceEntity, Object> approverRole;
	public static volatile SingularAttribute<ServiceEntity, Object> groupAdminRole;
	public static volatile SingularAttribute<ServiceEntity, Object> image;
	public static volatile CollectionAttribute<ServiceEntity, Object> policies;
	public static volatile CollectionAttribute<ServiceEntity, Object> attributeSourceService;
	public static volatile SingularAttribute<ServiceEntity, Object> accessRule;
	public static volatile SingularAttribute<ServiceEntity, Object> groupFilterRulePackage;
	public static volatile SingularAttribute<ServiceEntity, Object> mandatoryValueRulePackage;
}
