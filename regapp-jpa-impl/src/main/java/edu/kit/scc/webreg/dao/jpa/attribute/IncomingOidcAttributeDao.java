package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.IncomingOidcAttributeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class IncomingOidcAttributeDao extends JpaBaseDao<IncomingOidcAttributeEntity> {

	@Override
	public Class<IncomingOidcAttributeEntity> getEntityClass() {
		return IncomingOidcAttributeEntity.class;
	}
}
