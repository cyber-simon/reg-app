package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OidcRpFlowStateEntity.class)
public abstract class OidcRpFlowStateEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<OidcRpFlowStateEntity, String> state;
	public static volatile SingularAttribute<OidcRpFlowStateEntity, String> code;
	public static volatile SingularAttribute<OidcRpFlowStateEntity, Date> validUntil;
}

