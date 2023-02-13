package edu.kit.scc.webreg.dto.service;

import java.util.List;

import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface SshPubKeyDtoService extends BaseDtoService<SshPubKeyEntity, SshPubKeyEntityDto> {

	List<SshPubKeyEntityDto> findByUidNumber(Integer uidNumber) throws RestInterfaceException;

	List<SshPubKeyEntityDto> findByUidNumberAndStatus(Integer uidNumber, SshPubKeyStatus keyStatus)
			throws RestInterfaceException;

	List<SshPubKeyEntityDto> findByUidNumberAndExpiryInDays(Integer uidNumber, Integer days)
			throws RestInterfaceException;

	List<SshPubKeyEntityDto> findByExpiryInDays(Integer days) throws RestInterfaceException;

}
