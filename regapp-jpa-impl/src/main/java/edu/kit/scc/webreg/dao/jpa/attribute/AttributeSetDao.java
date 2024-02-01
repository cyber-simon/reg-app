package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.AttributeSetEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class AttributeSetDao extends JpaBaseDao<AttributeSetEntity> {

	@Override
	public Class<AttributeSetEntity> getEntityClass() {
		return AttributeSetEntity.class;
	}
}
