package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.entity.attribute.LocalAttributeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class LocalAttributeDao extends AttributeDao<LocalAttributeEntity> {

	@Override
	public Class<LocalAttributeEntity> getEntityClass() {
		return LocalAttributeEntity.class;
	}

}
