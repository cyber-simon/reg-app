package edu.kit.scc.webreg.service.attributes;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalUserAttributeSetDao;
import edu.kit.scc.webreg.entity.attribute.LocalUserAttributeSetEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class LocalUserAttributeSetService extends BaseServiceImpl<LocalUserAttributeSetEntity> {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private LocalUserAttributeSetDao dao;
	
	@Override
	protected LocalUserAttributeSetDao getDao() {
		return dao;
	}

}
