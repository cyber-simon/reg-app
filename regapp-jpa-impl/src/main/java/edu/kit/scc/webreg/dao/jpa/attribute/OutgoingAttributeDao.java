package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.entity.attribute.OutgoingAttributeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class OutgoingAttributeDao extends AttributeDao<OutgoingAttributeEntity> {

	@Override
	public Class<OutgoingAttributeEntity> getEntityClass() {
		return OutgoingAttributeEntity.class;
	}

}
