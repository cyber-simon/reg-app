package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ApplicationConfigEntity.class)
public abstract class ApplicationConfigEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<ApplicationConfigEntity, Boolean> activeConfig;
	public static volatile SingularAttribute<ApplicationConfigEntity, String> configFormatVersion;
	public static volatile SingularAttribute<ApplicationConfigEntity, String> subVersion;
	public static volatile SingularAttribute<ApplicationConfigEntity, Date> dirtyStamp;
	public static volatile MapAttribute<ApplicationConfigEntity, String, String> configOptions;

}

