package edu.kit.scc.webreg.dto.service;

import java.util.List;

import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface SshPubKeyDtoService extends BaseDtoService<SshPubKeyEntity, SshPubKeyEntityDto> {

	List<SshPubKeyEntityDto> findByUidNumber(Long uidNumber) throws RestInterfaceException;

	List<SshPubKeyEntityDto> findByUidNumberAndStatus(Long uidNumber, SshPubKeyStatus keyStatus)
			throws RestInterfaceException;

	List<SshPubKeyEntityDto> findByUidNumberAndExpiryInDays(Long uidNumber, Integer days)
			throws RestInterfaceException;

	List<SshPubKeyEntityDto> findByExpiryInDays(Integer days)
			throws RestInterfaceException;

}
