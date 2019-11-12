package edu.kit.scc.nextcloud;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public class NextcloudWorker {

	protected static Logger logger = LoggerFactory.getLogger(NextcloudWorker.class);

	private PropertyReader prop;
	private Auditor auditor;
	
	private CloseableHttpClient httpClient;
	
	private String apiUrl;
	private String apiUsername;
	private String apiPassword;

	public NextcloudWorker(PropertyReader prop) throws RegisterException {
		this(prop, null);
	}
	
	public NextcloudWorker(PropertyReader prop, Auditor auditor) throws RegisterException {
		this.prop = prop;
		this.auditor = auditor;
		
		try {
			apiUrl = prop.readProp("api_url");
			apiUsername = prop.readProp("api_username");
			apiPassword = prop.readProp("api_password");
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		}

		buildClient();
	}

	protected void buildClient() {
		RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000)
				.setConnectTimeout(5000).build();
		httpClient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
	}

	protected HttpClientContext buildHttpContext(URI uri) {
		HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
		AuthCache authCache = new BasicAuthCache();
		authCache.put(targetHost, new BasicScheme());

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(apiUsername, apiPassword);
		credsProvider.setCredentials(AuthScope.ANY, credentials);

		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);
		
		return context;
	}
	
	protected HttpGet buildHttpGet(URI uri) {
		HttpGet http = new HttpGet(uri);
		http.setHeader("OCS-APIRequest", "true");
		http.addHeader("Content-Type", "application/x-www-form-urlencoded");
		return http;
	}
	
	protected HttpPost buildHttpPost(URI uri) {
		HttpPost http = new HttpPost(uri);
		http.setHeader("OCS-APIRequest", "true");
		http.addHeader("Content-Type", "application/x-www-form-urlencoded");
		return http;
	}
	
	public NextcloudAnswer createAccount(RegistryEntity registry) throws RegisterException {

		String id = registry.getRegistryValues().get("id");
		String displayName = registry.getRegistryValues().get("displayName");
		String email = registry.getRegistryValues().get("email");
		
		URI uri;
		try {
			URIBuilder uriBuilder = new URIBuilder(apiUrl + "users");
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new RegisterException(e);
		}

		HttpClientContext context = buildHttpContext(uri);
		
		CloseableHttpResponse response;

		try {
			HttpPost http = buildHttpPost(uri);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("userid", id));
		    params.add(new BasicNameValuePair("displayName", displayName));
		    params.add(new BasicNameValuePair("email", email));
		    http.setEntity(new UrlEncodedFormEntity(params));
			
			response = httpClient.execute(http, context);
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
		} else if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Status answer was not HTTP OK 200");
			throw new RegisterException("Nexcloud: " + response.getStatusLine());
		}

		HttpEntity entity = response.getEntity();

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NextcloudAnswer.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			NextcloudAnswer answer = (NextcloudAnswer) unmarshaller.unmarshal(entity.getContent());

			logger.debug("{} {}", answer.getMeta().getStatusCode(), answer.getMeta().getStatus());
			
			return answer;
		} catch (ParseException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection", e);
			throw new RegisterException(e);
		} catch (IllegalStateException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (JAXBException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		}		
	}
	
	public NextcloudAnswer loadAccount(RegistryEntity registry) throws RegisterException {

		String id = registry.getRegistryValues().get("id");

		URI uri;
		try {
			URIBuilder uriBuilder = new URIBuilder(apiUrl + "users/" + id);
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new RegisterException(e);
		}

		HttpClientContext context = buildHttpContext(uri);
		
		HttpGet httpGet = buildHttpGet(uri);

		CloseableHttpResponse response;
		try {
			response = httpClient.execute(httpGet, context);
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
		} else if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Status answer was not HTTP OK 200");
			throw new RegisterException("Nexcloud: " + response.getStatusLine());
		}

		HttpEntity entity = response.getEntity();

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NextcloudAnswer.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			NextcloudAnswer answer = (NextcloudAnswer) unmarshaller.unmarshal(entity.getContent());

			logger.debug("{} {}", answer.getMeta().getStatusCode(), answer.getMeta().getStatus());
			
			return answer;
		} catch (ParseException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection", e);
			throw new RegisterException(e);
		} catch (IllegalStateException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (JAXBException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		}
	}
	
	public NextcloudAnswer createGroup(String groupName) throws RegisterException {

		URI uri;
		try {
			URIBuilder uriBuilder = new URIBuilder(apiUrl + "groups");
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new RegisterException(e);
		}

		HttpClientContext context = buildHttpContext(uri);
		
		CloseableHttpResponse response;

		try {
			HttpPost http = buildHttpPost(uri);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("groupid", groupName));
		    http.setEntity(new UrlEncodedFormEntity(params));
			
			response = httpClient.execute(http, context);
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
		} else if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Status answer was not HTTP OK 200");
			throw new RegisterException("Nexcloud: " + response.getStatusLine());
		}

		HttpEntity entity = response.getEntity();

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NextcloudAnswer.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			NextcloudAnswer answer = (NextcloudAnswer) unmarshaller.unmarshal(entity.getContent());

			logger.debug("{} {}", answer.getMeta().getStatusCode(), answer.getMeta().getStatus());
			
			return answer;
		} catch (ParseException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection", e);
			throw new RegisterException(e);
		} catch (IllegalStateException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (JAXBException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		}		
	}
	
	public NextcloudAnswer addUserToGroup(RegistryEntity registry, String groupName) throws RegisterException {

		String id = registry.getRegistryValues().get("id");
		
		URI uri;
		try {
			URIBuilder uriBuilder = new URIBuilder(apiUrl + "users/" + id + "/groups");
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new RegisterException(e);
		}

		HttpClientContext context = buildHttpContext(uri);
		
		CloseableHttpResponse response;

		try {
			HttpPost http = buildHttpPost(uri);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
		    params.add(new BasicNameValuePair("groupid", groupName));
		    http.setEntity(new UrlEncodedFormEntity(params));
			
			response = httpClient.execute(http, context);
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
		} else if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() != 200) {
			logger.warn("Status answer was not HTTP OK 200");
			throw new RegisterException("Nexcloud: " + response.getStatusLine());
		}

		HttpEntity entity = response.getEntity();

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NextcloudAnswer.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			NextcloudAnswer answer = (NextcloudAnswer) unmarshaller.unmarshal(entity.getContent());

			logger.debug("{} {}", answer.getMeta().getStatusCode(), answer.getMeta().getStatus());
			
			return answer;
		} catch (ParseException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (IOException e) {
			logger.warn("Connection", e);
			throw new RegisterException(e);
		} catch (IllegalStateException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		} catch (JAXBException e) {
			logger.warn("Parse problem", e);
			throw new RegisterException(e);
		}		
	}	
}
