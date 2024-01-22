package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.LocalAttributeSetEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class LocalAttributeSetDao extends JpaBaseDao<LocalAttributeSetEntity> {

	@Override
	public Class<LocalAttributeSetEntity> getEntityClass() {
		return LocalAttributeSetEntity.class;
	}
}
