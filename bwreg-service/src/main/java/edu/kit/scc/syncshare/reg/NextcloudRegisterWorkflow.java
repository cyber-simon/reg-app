package edu.kit.scc.syncshare.reg;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.nextcloud.NextcloudAnswer;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

public class NextcloudRegisterWorkflow  implements RegisterUserWorkflow, InfotainmentCapable {

	private static final Logger logger = LoggerFactory.getLogger(NextcloudRegisterWorkflow.class);

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user, ServiceEntity service)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		
		String id = registry.getRegistryValues().get("id");
		
		String apiUrl;
		String apiUsername;
		String apiPassword;
		
		try {
			apiUrl = prop.readProp("api_url");
			apiUsername = prop.readProp("api_username");
			apiPassword = prop.readProp("api_password");
		} catch (PropertyReaderException e) {
			throw new RegisterException(e);
		}
		
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(5000)
				.setConnectTimeout(5000)
				.build();
		CloseableHttpClient httpclient = HttpClients.custom()
			    .setDefaultRequestConfig(defaultRequestConfig)
			    .build();

		URI uri;
		try {
			URIBuilder uriBuilder = new URIBuilder(apiUrl + "users/" + id);
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new RegisterException(e);
		}
		
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(apiUsername, apiPassword);
        credsProvider.setCredentials(AuthScope.ANY, credentials);
		
		HttpClientContext context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setHeader("OCS-APIRequest", "true");
		httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");

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
			throw new RegisterException("Nexcloud: " + response.getStatusLine());
		}
		
		HttpEntity entity = response.getEntity();

		Infotainment info = new Infotainment();
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(NextcloudAnswer.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			NextcloudAnswer answer = (NextcloudAnswer) unmarshaller.unmarshal(entity.getContent());

			info.setMessage("Account geladen");
			info.setRoot(root);
			InfotainmentTreeNode node = new InfotainmentTreeNode("Status", root);
			new InfotainmentTreeNode("StatusCode", "" + answer.getMeta().getStatusCode(), node);
			new InfotainmentTreeNode("Status", answer.getMeta().getStatus(), node);
			new InfotainmentTreeNode("Message", answer.getMeta().getMessage(), node);

			node = new InfotainmentTreeNode("User Info", root);
			if (answer.getUser() != null) {
				new InfotainmentTreeNode("ID", answer.getUser().getId(), node);
				new InfotainmentTreeNode("Name", answer.getUser().getDisplayName(), node);
				new InfotainmentTreeNode("E-Mail", answer.getUser().getEmail(), node);
				if (answer.getUser().getQuota() != null) {
					new InfotainmentTreeNode("Verbrauchter Platz", "" +  answer.getUser().getQuota().getRelative() + "%", node);
				}
			}

			logger.debug("{} {}", answer.getMeta().getStatusCode(), answer.getMeta().getStatus());
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
		
		
		return info;
	}

	@Override
	public void registerUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		
		
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		
	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service, RegistryEntity registry, Auditor auditor)
			throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		String tplId = prop.readPropOrNull("tpl_id");
		String id;
		if (tplId != null) {
			id = evalTemplate(tplId, user);
		} 
		else {
			id = user.getEppn();
		}

		if (! registry.getRegistryValues().containsKey("id")) {
			registry.getRegistryValues().put("id", id);
		} else {
			if (! registry.getRegistryValues().get("id").equals(id)) {
				// this should not happen. It means the primary Id for the user has changed. 
				// Nextcloud saml does not support this
				logger.warn("Nextcloud User ID for user {} would chang from {} to {}!", registry.getRegistryValues().get("id"), id);
			}
		}

		return false;
	}
	
	protected String evalTemplate(String template, UserEntity user) 
			throws RegisterException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.init();
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", user);
		VelocityContext velocityContext = new VelocityContext(context);
		StringWriter out = new StringWriter();

		try {
			engine.evaluate(velocityContext, out, "log", template);
			
			return out.toString();
		} catch (ParseErrorException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		} catch (MethodInvocationException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		} catch (ResourceNotFoundException e) {
			logger.warn("Velocity problem: {}", e.getMessage());
			throw new RegisterException(e);
		}
	}

}
