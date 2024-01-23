package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;

public class AttributeReleaseDao extends JpaBaseDao<AttributeReleaseEntity> {

	@Override
	public Class<AttributeReleaseEntity> getEntityClass() {
		return AttributeReleaseEntity.class;
	}
}
