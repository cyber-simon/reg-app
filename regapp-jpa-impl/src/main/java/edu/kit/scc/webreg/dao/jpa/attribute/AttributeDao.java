package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.AttributeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class AttributeDao extends JpaBaseDao<AttributeEntity> {

	@Override
	public Class<AttributeEntity> getEntityClass() {
		return AttributeEntity.class;
	}
}
