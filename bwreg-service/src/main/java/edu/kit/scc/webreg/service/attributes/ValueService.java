package edu.kit.scc.webreg.service.attributes;

import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ValueService extends BaseServiceImpl<ValueEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private ValueDao dao;
	
	@Override
	protected ValueDao getDao() {
		return dao;
	}
}
