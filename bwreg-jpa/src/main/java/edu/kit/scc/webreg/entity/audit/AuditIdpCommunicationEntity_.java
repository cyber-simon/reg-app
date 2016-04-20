package edu.kit.scc.webreg.entity.audit;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AuditIdpCommunicationEntity.class)
public abstract class AuditIdpCommunicationEntity_ extends edu.kit.scc.webreg.entity.audit.AuditEntryEntity_ {

	public static volatile SingularAttribute<AuditIdpCommunicationEntity, SamlIdpMetadataEntity> idp;
	public static volatile SingularAttribute<AuditIdpCommunicationEntity, SamlSpConfigurationEntity> spConfig;

}

