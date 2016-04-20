package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SamlIdpMetadataEntity.class)
public abstract class SamlIdpMetadataEntity_ extends edu.kit.scc.webreg.entity.SamlMetadataEntity_ {

	public static volatile ListAttribute<SamlIdpMetadataEntity, String> entityCategoryList;
	public static volatile SingularAttribute<SamlIdpMetadataEntity, Date> lastIdStatusChange;
	public static volatile SetAttribute<SamlIdpMetadataEntity, FederationEntity> federations;
	public static volatile SetAttribute<SamlIdpMetadataEntity, SamlIdpScopeEntity> scopes;
	public static volatile SingularAttribute<SamlIdpMetadataEntity, Date> lastAqStatusChange;
	public static volatile SingularAttribute<SamlIdpMetadataEntity, SamlIdpMetadataEntityStatus> aqIdpStatus;
	public static volatile SingularAttribute<SamlIdpMetadataEntity, SamlIdpMetadataEntityStatus> idIdpStatus;

}

