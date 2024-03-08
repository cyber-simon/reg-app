package edu.kit.scc.webreg.service.oidc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.jpa.oidc.OidcRedirectUrlDao;
import edu.kit.scc.webreg.entity.oidc.OidcClientConsumerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRedirectUrlEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRedirectUrlEntity_;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class OidcRedirectUrlService extends BaseServiceImpl<OidcRedirectUrlEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcRedirectUrlDao dao;

	public Set<String> getRedirectsForClient(OidcClientConsumerEntity client) {
		return dao.findAll(equal(OidcRedirectUrlEntity_.client, client)).stream().map(r -> r.getUrl())
				.collect(Collectors.toCollection(HashSet<String>::new));
	}

	public void saveRedirectsForClient(Set<String> redirects, OidcClientConsumerEntity client) {
		dao.findAll(equal(OidcRedirectUrlEntity_.client, client)).stream().forEach(r -> dao.delete(r));
		redirects.stream().forEach(r -> addUrl(r, client));
	}
	
	public OidcRedirectUrlEntity addUrl(String url, OidcClientConsumerEntity client) {
		OidcRedirectUrlEntity redirectUrl = dao.createNew();
		redirectUrl.setClient(client);
		redirectUrl.setUrl(url);
		return dao.persist(redirectUrl);
	}
	
	public void deleteUrl(String url, OidcClientConsumerEntity client) {
		OidcRedirectUrlEntity redirectUrl = dao
				.find(and(equal(OidcRedirectUrlEntity_.client, client), equal(OidcRedirectUrlEntity_.url, url)));
		dao.delete(redirectUrl);
	}

	@Override
	protected BaseDao<OidcRedirectUrlEntity> getDao() {
		return dao;
	}
}
