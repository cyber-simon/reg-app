package edu.kit.scc.webreg.dao.jpa;

import edu.kit.scc.webreg.entity.IconCacheEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class IconCacheDao extends JpaBaseDao<IconCacheEntity> {

	@Override
	public Class<IconCacheEntity> getEntityClass() {
		return IconCacheEntity.class;
	}
}
