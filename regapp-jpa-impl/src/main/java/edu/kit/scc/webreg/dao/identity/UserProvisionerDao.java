package edu.kit.scc.webreg.dao.identity;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class UserProvisionerDao extends JpaBaseDao<UserProvisionerEntity> {

	@Override
	public Class<UserProvisionerEntity> getEntityClass() {
		return UserProvisionerEntity.class;
	}
}
