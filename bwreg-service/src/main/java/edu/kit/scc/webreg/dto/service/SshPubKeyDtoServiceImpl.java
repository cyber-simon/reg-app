package edu.kit.scc.webreg.dto.service;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThan;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.SshPubKeyEntityMapper;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyEntity_;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Stateless
public class SshPubKeyDtoServiceImpl extends BaseDtoServiceImpl<SshPubKeyEntity, SshPubKeyEntityDto>
		implements SshPubKeyDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserDao userDao;

	@Inject
	private SshPubKeyDao dao;

	@Inject
	private SshPubKeyEntityMapper mapper;

	@Override
	public List<SshPubKeyEntityDto> findByUidNumber(Integer uidNumber) throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);

		List<SshPubKeyEntity> list = dao.findAll(equal("identity.id", user.getIdentity().getId()));

		return convertList(list);
	}

	@Override
	public List<SshPubKeyEntityDto> findByUidNumberAndStatus(Integer uidNumber, SshPubKeyStatus keyStatus)
			throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);

		List<SshPubKeyEntity> list = dao.findAll(
				and(equal("identity.id", user.getIdentity().getId()), equal(SshPubKeyEntity_.keyStatus, keyStatus)));

		return convertList(list);
	}

	@Override
	public List<SshPubKeyEntityDto> findByUidNumberAndExpiryInDays(Integer uidNumber, Integer days)
			throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);

		List<SshPubKeyEntity> list = findKeysThatExpireWithinNDaysByIdentity(user.getIdentity().getId(), days);

		return convertList(list);
	}

	private List<SshPubKeyEntity> findKeysThatExpireWithinNDaysByIdentity(Long identityId, Integer days) {
		return dao.findAll(
				and(equal("identity.id", identityId), lessThan(SshPubKeyEntity_.expiresAt, nowInNDays(days))));
	}

	private Date nowInNDays(Integer days) {
		return Date.from(Instant.now().plus(days, DAYS));
	}

	@Override
	public List<SshPubKeyEntityDto> findByExpiryInDays(Integer days) throws RestInterfaceException {
		List<SshPubKeyEntity> list = findKeysThatExpireWithinNDays(days);

		return convertList(list);
	}

	private List<SshPubKeyEntity> findKeysThatExpireWithinNDays(Integer days) {
		return dao.findAll(lessThan(SshPubKeyEntity_.expiresAt, nowInNDays(days)));
	}

	protected List<SshPubKeyEntityDto> convertList(List<SshPubKeyEntity> list) {
		List<SshPubKeyEntityDto> dtoList = new ArrayList<SshPubKeyEntityDto>(list.size());

		for (SshPubKeyEntity key : list) {
			SshPubKeyEntityDto dto = createNewDto();
			mapper.copyProperties(key, dto);
			dtoList.add(dto);
		}

		return dtoList;
	}

	@Override
	protected BaseEntityMapper<SshPubKeyEntity, SshPubKeyEntityDto> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<SshPubKeyEntity> getDao() {
		return dao;
	}

}
