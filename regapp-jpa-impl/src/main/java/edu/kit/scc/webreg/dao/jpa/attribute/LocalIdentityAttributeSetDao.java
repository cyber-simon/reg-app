package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class LocalIdentityAttributeSetDao extends JpaBaseDao<LocalIdentityAttributeSetEntity> {

	@Override
	public Class<LocalIdentityAttributeSetEntity> getEntityClass() {
		return LocalIdentityAttributeSetEntity.class;
	}
}
