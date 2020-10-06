package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;

@Singleton
public class OidcOpMetadataSingletonBean {

	@Inject
	private Logger logger;
	
	private Map<OidcRpConfigurationEntity, OIDCProviderMetadata> metadataMap;
	private Map<OidcRpConfigurationEntity, Long> expiryMap;
	
	public OidcOpMetadataSingletonBean() {
		metadataMap = new HashMap<OidcRpConfigurationEntity, OIDCProviderMetadata>();
		expiryMap = new HashMap<OidcRpConfigurationEntity, Long>();
	}
	
	public URI getAuthorizationEndpointURI(OidcRpConfigurationEntity rpConfig) throws IOException, ParseException {
		OIDCProviderMetadata opMetadata = getOpMetadata(rpConfig);
		return opMetadata.getAuthorizationEndpointURI();
	}

	public URI getTokenEndpointURI(OidcRpConfigurationEntity rpConfig) throws IOException, ParseException {
		OIDCProviderMetadata opMetadata = getOpMetadata(rpConfig);
		return opMetadata.getTokenEndpointURI();		
	}
	
	public URI getUserInfoEndpointURI(OidcRpConfigurationEntity rpConfig) throws IOException, ParseException {
		OIDCProviderMetadata opMetadata = getOpMetadata(rpConfig);
		return opMetadata.getUserInfoEndpointURI();		
	}
	
	public OIDCProviderMetadata getOpMetadata(OidcRpConfigurationEntity rpConfig) throws IOException, ParseException {
		OIDCProviderMetadata opMetadata;

		Boolean expired = false;
		synchronized (expiryMap) {
			if (expiryMap.containsKey(rpConfig)) {
				if ((System.currentTimeMillis() - expiryMap.get(rpConfig)) > 5 * 60 * 1000L) {
					// metadata is more than 5 minutes old. Set to expired
					expired = true;
				}
			}
			else {
				// metadata not loaded yet. Set to expired
				expired = true;
			}
		}
		
		if (expired) {
			logger.debug("Reloading metadata for {}", rpConfig.getName());
			
			Issuer issuer = new Issuer(rpConfig.getServiceUrl());
			OIDCProviderConfigurationRequest configRequest = new OIDCProviderConfigurationRequest(issuer);
			HTTPResponse configResponse = configRequest.toHTTPRequest().send();
			
			opMetadata = OIDCProviderMetadata.parse(configResponse.getContentAsJSONObject());
			
			synchronized (expiryMap) {
				metadataMap.put(rpConfig, opMetadata);
				expiryMap.put(rpConfig, System.currentTimeMillis());
			}
		}
		else {
			synchronized (expiryMap) {
				opMetadata = metadataMap.get(rpConfig);
			}			
		}
		
		return opMetadata;
	}
}
