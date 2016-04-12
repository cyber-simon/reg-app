package edu.kit.scc.webreg.entity.audit;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

@StaticMetamodel(AuditUserUpdateEntity.class)
public class AuditIdpCommunicationEntity_ {
	public static volatile SingularAttribute<AuditIdpCommunicationEntity, SamlIdpMetadataEntity> idp;
	public static volatile SingularAttribute<AuditIdpCommunicationEntity, SamlSpConfigurationEntity> spConfig;
}
