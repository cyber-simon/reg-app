package edu.kit.scc.webreg.entity.oidc;

import java.util.Date;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OidcFlowStateEntity.class)
public abstract class OidcFlowStateEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<OidcFlowStateEntity, String> nonce;
	public static volatile SingularAttribute<OidcFlowStateEntity, String> state;
	public static volatile SingularAttribute<OidcFlowStateEntity, String> code;
	public static volatile SingularAttribute<OidcFlowStateEntity, String> accessToken;
	public static volatile SingularAttribute<OidcFlowStateEntity, String> accessTokenType;
	public static volatile SingularAttribute<OidcFlowStateEntity, Date> validUntil;
}

