package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class ValueDao extends JpaBaseDao<ValueEntity> {

	@Override
	public Class<ValueEntity> getEntityClass() {
		return ValueEntity.class;
	}
}
