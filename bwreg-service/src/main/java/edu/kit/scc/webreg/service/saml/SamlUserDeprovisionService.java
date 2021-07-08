package edu.kit.scc.webreg.service.saml;

import java.io.Serializable;
import java.util.List;

import edu.kit.scc.webreg.entity.SamlUserEntity;

public interface SamlUserDeprovisionService extends Serializable {

	List<SamlUserEntity> findUsersForPseudo(Long onHoldSince, int limit);
	SamlUserEntity pseudoUser(SamlUserEntity user);
}
