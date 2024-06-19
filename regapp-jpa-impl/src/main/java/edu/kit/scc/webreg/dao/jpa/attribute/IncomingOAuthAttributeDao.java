package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.entity.attribute.IncomingOAuthAttributeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class IncomingOAuthAttributeDao extends IncomingAttributeDao<IncomingOAuthAttributeEntity> {

	@Override
	public Class<IncomingOAuthAttributeEntity> getEntityClass() {
		return IncomingOAuthAttributeEntity.class;
	}
}
