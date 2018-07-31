package edu.kit.scc.webreg.dto.service;

import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface GroupDtoService extends BaseDtoService<GroupEntity, GroupEntityDto, Long> {

	GroupEntityDto findByName(String name, Long userId) throws RestInterfaceException ;

	GroupEntityDto findById(Long id, Long userId) throws RestInterfaceException;


}
