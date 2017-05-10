/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.lsdf.sns.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public class PFWorker {

	private static final Logger logger = LoggerFactory.getLogger(PFWorker.class);
	
	private Auditor auditor;
	
	private PFAccountSerializer accountSerializer;
	
	private String pfApiUrl;
	private String pfApiUsername;
	private String pfApiPassword;

	private CloseableHttpClient httpclient;
	
	public PFWorker(PropertyReader prop) throws RegisterException {
		this(prop, null);
	}
	
	public PFWorker(PropertyReader prop, Auditor auditor) throws RegisterException {
		this.auditor = auditor;
		
		try {
			pfApiUrl = prop.readProp("pf_api_url");
			pfApiUsername = prop.readProp("pf_api_username");
			pfApiPassword = prop.readProp("pf_api_password");
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		}

		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(5000)
				.setConnectTimeout(5000)
				.build();
		httpclient = HttpClients.custom()
			    .setDefaultRequestConfig(defaultRequestConfig)
			    .build();
		
		accountSerializer = new PFAccountSerializer();
	}

	@SuppressWarnings("unchecked")
	public PFAccount storeAccount(PFAccount pfAccount) throws RegisterException {
		
		Map<String, String> parameterMap = accountSerializer.marshal(pfAccount); 
		parameterMap.put("action", "store");

		String s = executeGet(parameterMap);
		
		if (s == null) {
			logger.warn("Account not found: {}", pfAccount.getId());
			throw new RegisterException("account not found");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> userData;
		try {
			userData = mapper.readValue(s, Map.class);
		} catch (JsonParseException e) {
			logger.warn("JSON parse problem: {}", e.toString());
			// Means no Account was found
			throw new RegisterException(e);
		} catch (JsonMappingException e) {
			logger.warn("JSON Mapping problem: {}", e.toString());
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection problem: {}", e.toString());
			throw new RegisterException(e);
		}
		
		if (auditor != null) {
			if (s.length() > 1020)
				s = s.substring(0, 1020);
			auditor.logAction(pfAccount.getId(), "STORE ACCOUNT", pfAccount.getUsername(), s, AuditStatus.SUCCESS);
		}
		
		return accountSerializer.unmarshal(userData);		
	}
	
	public PFAccount getAccountInfoById(String id) throws RegisterException {

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("action", "getInfo");
		parameterMap.put("ID", id);
		
		return getAccountInfo(parameterMap);
	}

	public PFAccount getAccountInfoByUsername(String username) throws RegisterException {

		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("action", "getInfo");
		parameterMap.put("username", username);
		
		return getAccountInfo(parameterMap);
	}

	@SuppressWarnings("unchecked")
	protected PFAccount getAccountInfo(Map<String, String> parameterMap) throws RegisterException {

		String s = executeGet(parameterMap);

		if (s == null) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> userData;
		try {
			userData = mapper.readValue(s, Map.class);
		} catch (JsonParseException e) {
			logger.warn("JSON parse problem: {}", e.toString());
			// Means no Account was found
			return null;
		} catch (JsonMappingException e) {
			logger.warn("JSON Mapping problem: {}", e.toString());
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection problem: {}", e.toString());
			throw new RegisterException(e);
		}
		
		return accountSerializer.unmarshal(userData);
	}
	
	protected String executeGet(Map<String, String> parameterMap) throws RegisterException {
		URI uri = buildUri(parameterMap);
		HttpClientContext context = buildHttpContext(uri);
		HttpGet httpGet = new HttpGet(uri);

		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpGet, context);
		} catch (ClientProtocolException e) {
			logger.warn("Client protocol problem", e);
			throw new RegisterException(e);
		} catch (SSLException e) {
			logger.error("SSL Certificate problem with SNS Server: {}", e.toString());
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection", e);
			throw new RegisterException(e);
		}
		
		logger.debug("Status line of response: {}", response.getStatusLine());
		
		if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 404) {
			logger.warn("Status answer is 404, Account not found.");
			return null;
		}
		else if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Status answer was not HTTP OK 200");
			throw new RegisterException("Powerfolder: " + response.getStatusLine());
		}
		
		HttpEntity entity = response.getEntity();

		String s;
		try {
			s = EntityUtils.toString(entity);
		} catch (ParseException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection", e);
			throw new RegisterException(e);
		}
		
		return s;
	}
	
	protected URI buildUri(Map<String, String> parameterMap) throws RegisterException {
		try {
			URIBuilder uriBuilder = new URIBuilder(pfApiUrl + "/accounts");
			for (Entry<String, String> parameter : parameterMap.entrySet()) {
				uriBuilder.addParameter(parameter.getKey(), parameter.getValue());
			}
			return uriBuilder.build();
		} catch (URISyntaxException e) {
			logger.error("Service is misconfigured. URI is not valid", e);
			throw new RegisterException("Service is misconfigured. URI is not valid");
		}
	}
	
	protected HttpClientContext buildHttpContext(URI uri) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(uri.getHost(),
				AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(pfApiUsername, pfApiPassword));
		
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		
		return context;
	}
}
