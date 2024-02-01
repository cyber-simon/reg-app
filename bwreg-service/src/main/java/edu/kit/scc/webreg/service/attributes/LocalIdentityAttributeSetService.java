package edu.kit.scc.webreg.service.attributes;

import edu.kit.scc.webreg.dao.jpa.attribute.LocalIdentityAttributeSetDao;
import edu.kit.scc.webreg.entity.attribute.LocalIdentityAttributeSetEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.inject.Inject;

public class LocalIdentityAttributeSetService extends BaseServiceImpl<LocalIdentityAttributeSetEntity> {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private LocalIdentityAttributeSetDao dao;
	
	@Override
	protected LocalIdentityAttributeSetDao getDao() {
		return dao;
	}

}
