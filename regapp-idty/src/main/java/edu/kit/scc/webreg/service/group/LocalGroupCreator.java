package edu.kit.scc.webreg.service.group;

import java.io.Serializable;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.GroupStatus;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceAutoconnectGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;

@ApplicationScoped
public class LocalGroupCreator implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private LocalGroupDao localGroupDao;

	@Inject
	private ServiceGroupFlagDao groupFlagDao;

	@Inject
	private SerialDao serialDao;

	@Inject
	private ServiceDao serviceDao;
	
	public LocalGroupEntity createNew(ServiceEntity service) {
		return createNew(null, service);
	}

	public LocalGroupEntity createNew(String name, ServiceEntity service) {
		LocalGroupEntity entity = localGroupDao.createNew();

		entity.setName(name);
		entity.setAdminRoles(new HashSet<RoleEntity>());
		entity.setParents(new HashSet<GroupEntity>());
		entity.setChildren(new HashSet<GroupEntity>());
		entity.getAdminRoles().add(service.getGroupAdminRole());

		return entity;
	}
	
	public LocalGroupEntity save(LocalGroupEntity entity, ServiceEntity service) {
		service = serviceDao.findById(service.getId());
		
		entity.setGidNumber(serialDao.next("gid-number-serial").intValue());
		entity.setGroupStatus(GroupStatus.ACTIVE);
		
		entity = localGroupDao.persist(entity);
		
		ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
		groupFlag.setService(service);
		groupFlag.setGroup(entity);
		groupFlag.setStatus(ServiceGroupStatus.CLEAN);
		
		groupFlag = groupFlagDao.persist(groupFlag);

		if (service.getGroupAutoconnectServices() != null) {
			for (ServiceAutoconnectGroupEntity sink : service.getGroupAutoconnectServices()) {
				if (entity.getName().matches(sink.getFilterRegex())) {
					groupFlag = groupFlagDao.createNew();
					groupFlag.setService(sink.getToService());
					groupFlag.setGroup(entity);
					groupFlag.setStatus(ServiceGroupStatus.CLEAN);
					
					groupFlag = groupFlagDao.persist(groupFlag);
				}
			}
		}
		
		return entity;
	}
}
