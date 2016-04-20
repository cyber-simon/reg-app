package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BusinessRuleEntity.class)
public abstract class BusinessRuleEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<BusinessRuleEntity, BusinessRulePackageEntity> rulePackage;
	public static volatile SingularAttribute<BusinessRuleEntity, String> ruleType;
	public static volatile SingularAttribute<BusinessRuleEntity, String> name;
	public static volatile SingularAttribute<BusinessRuleEntity, String> rule;
	public static volatile SingularAttribute<BusinessRuleEntity, String> knowledgeBaseName;

}

