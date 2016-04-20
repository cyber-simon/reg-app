package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(EmailTemplateEntity.class)
public abstract class EmailTemplateEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<EmailTemplateEntity, String> cc;
	public static volatile SingularAttribute<EmailTemplateEntity, String> bcc;
	public static volatile SingularAttribute<EmailTemplateEntity, String> subject;
	public static volatile SingularAttribute<EmailTemplateEntity, String> name;
	public static volatile SingularAttribute<EmailTemplateEntity, String> from;
	public static volatile SingularAttribute<EmailTemplateEntity, String> to;
	public static volatile SingularAttribute<EmailTemplateEntity, String> body;

}

