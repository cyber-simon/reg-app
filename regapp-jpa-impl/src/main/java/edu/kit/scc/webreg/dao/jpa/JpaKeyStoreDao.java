package edu.kit.scc.webreg.dao.jpa;

import edu.kit.scc.webreg.dao.KeyStoreDao;
import edu.kit.scc.webreg.entity.KeyStoreEntity;

public class JpaKeyStoreDao extends JpaBaseDao<KeyStoreEntity> implements KeyStoreDao {

	@Override
	public Class<KeyStoreEntity> getEntityClass() {
		return KeyStoreEntity.class;
	}

}
