package edu.kit.scc.webreg.dto.service;

import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface GroupDtoService extends BaseDtoService<GroupEntity, GroupEntityDto, Long> {

	GroupEntityDto findByName(String name, Long userId, Boolean withDetails) throws RestInterfaceException ;

	GroupEntityDto findById(Long id, Long userId, Boolean withDetails) throws RestInterfaceException;

	GroupEntityDto create(String ssn, String name, Long userId) throws RestInterfaceException;

	GroupEntityDto addUserToGroup(Long groupId, Long userId, Long callerId) throws RestInterfaceException;

}
