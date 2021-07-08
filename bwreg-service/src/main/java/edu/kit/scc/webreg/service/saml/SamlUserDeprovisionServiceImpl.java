package edu.kit.scc.webreg.service.saml;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.entity.SamlUserEntity;

@Stateless
public class SamlUserDeprovisionServiceImpl implements SamlUserDeprovisionService {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(SamlUserDeprovisionServiceImpl.class);
	
	@Inject
	private SamlUserDao dao;
	
	@Override
	public List<SamlUserEntity> findUsersForPseudo(Long onHoldSince, int limit) {
		return dao.findUsersForPseudo(onHoldSince, limit);
	}
	
	@Override
	public SamlUserEntity pseudoUser(SamlUserEntity user) {
		user = dao.findById(user.getId());
		
		logger.info("Pseudonymisiong user {} (identity {})", user.getId(), user.getIdentity().getId());
		
		user.setEppn(null);
		user.setEmail(null);
		user.getEmailAddresses().clear();
		user.setGivenName(null);
		user.setSurName(null);
		user.getAssertions().clear();
		user.getGenericStore().put("pseudoed_since", new Date().toString());
		return user;
	}
}
