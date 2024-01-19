package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class IncomingAttributeSetDao extends JpaBaseDao<IncomingAttributeSetEntity> {

	@Override
	public Class<IncomingAttributeSetEntity> getEntityClass() {
		return IncomingAttributeSetEntity.class;
	}
}
