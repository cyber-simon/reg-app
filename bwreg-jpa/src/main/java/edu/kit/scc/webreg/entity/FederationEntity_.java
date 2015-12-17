package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:38.159+0100")
@StaticMetamodel(FederationEntity.class)
public class FederationEntity_ {
	public static volatile SingularAttribute<FederationEntity, Object> entityFilterRulePackage;
	public static volatile CollectionAttribute<FederationEntity, Object> idps;
	public static volatile CollectionAttribute<FederationEntity, Object> sps;
	public static volatile CollectionAttribute<FederationEntity, Object> aas;
}
