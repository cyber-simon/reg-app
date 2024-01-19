package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.IncomingSamlAttributeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class IncomingSamlAttributeDao extends JpaBaseDao<IncomingSamlAttributeEntity> {

	@Override
	public Class<IncomingSamlAttributeEntity> getEntityClass() {
		return IncomingSamlAttributeEntity.class;
	}
}
