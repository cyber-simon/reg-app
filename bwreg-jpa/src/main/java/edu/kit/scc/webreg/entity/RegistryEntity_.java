package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:44:17.457+0100")
@StaticMetamodel(RegistryEntity.class)
public class RegistryEntity_ {
	public static volatile SingularAttribute<RegistryEntity, UserEntity> user;
	public static volatile SingularAttribute<RegistryEntity, ServiceEntity> service;
	public static volatile CollectionAttribute<RegistryEntity, AgreementTextEntity> agreedTexts;
}
