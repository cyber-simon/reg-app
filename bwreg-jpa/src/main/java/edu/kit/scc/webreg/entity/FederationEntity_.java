package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FederationEntity.class)
public abstract class FederationEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<FederationEntity, Boolean> fetchIdps;
	public static volatile SetAttribute<FederationEntity, SamlAAMetadataEntity> aas;
	public static volatile SingularAttribute<FederationEntity, String> entityCategoryFilter;
	public static volatile SingularAttribute<FederationEntity, Date> polledAt;
	public static volatile SingularAttribute<FederationEntity, BusinessRulePackageEntity> entityFilterRulePackage;
	public static volatile SetAttribute<FederationEntity, SamlSpMetadataEntity> sps;
	public static volatile SingularAttribute<FederationEntity, String> name;
	public static volatile SingularAttribute<FederationEntity, String> entityId;
	public static volatile SingularAttribute<FederationEntity, String> federationMetadataUrl;
	public static volatile SingularAttribute<FederationEntity, Boolean> fetchAAs;
	public static volatile SingularAttribute<FederationEntity, Boolean> fetchSps;
	public static volatile SetAttribute<FederationEntity, SamlIdpMetadataEntity> idps;

}

