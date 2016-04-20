package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(BusinessRulePackageEntity.class)
public abstract class BusinessRulePackageEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<BusinessRulePackageEntity, String> knowledgeBaseVersion;
	public static volatile SingularAttribute<BusinessRulePackageEntity, String> knowledgeBaseName;
	public static volatile SetAttribute<BusinessRulePackageEntity, BusinessRuleEntity> rules;
	public static volatile SingularAttribute<BusinessRulePackageEntity, String> packageName;
	public static volatile SingularAttribute<BusinessRulePackageEntity, Date> dirtyStamp;

}

