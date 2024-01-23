package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class LocalUserAttributeSetDao extends JpaBaseDao<LocalUserAttributeSetEntity> {

	@Override
	public Class<LocalUserAttributeSetEntity> getEntityClass() {
		return LocalUserAttributeSetEntity.class;
	}
}
