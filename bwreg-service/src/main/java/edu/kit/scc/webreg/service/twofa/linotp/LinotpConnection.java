package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.twofa.TwoFaException;

public class LinotpConnection {

	private static final Logger logger = LoggerFactory.getLogger(LinotpConnection.class);
	
	private Map<String, String> configMap;

	private LinotpResultParser resultParser;
	
	private URI uri;
	private HttpHost targetHost;
	private AuthCache authCache;
	private CredentialsProvider credsProvider;
	private RequestConfig config;
	private CloseableHttpClient httpClient;
	private HttpClientContext context;
	
	private String adminSession;
	
	public LinotpConnection(Map<String, String> configMap) throws TwoFaException {
		super();
		this.configMap = configMap;
		resultParser = new LinotpResultParser();
		init();
	}
	
	public void close() {
		try {
			httpClient.close();
		} catch (IOException e) {
		}
	}
	
	protected void init() throws TwoFaException {
		try {
			uri = new URI(configMap.get("url"));
		} catch (URISyntaxException e) {
			throw new TwoFaException(e);
		}
		targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
		authCache = new BasicAuthCache();
		authCache.put(targetHost, new BasicScheme());

		credsProvider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(configMap.get("username"), configMap.get("password"));
		credsProvider.setCredentials(AuthScope.ANY, credentials);

		context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);

		config = RequestConfig.custom()
			    .setSocketTimeout(5000)
			    .setConnectTimeout(5000)
			    .build();
		httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
	}
	
	public LinotpInitAuthenticatorTokenResponse createAuthenticatorToken(UserEntity user) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/init");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("type", "totp"));
			nvps.add(new BasicNameValuePair("otplen", "6"));
			nvps.add(new BasicNameValuePair("genkey", "1"));
			nvps.add(new BasicNameValuePair("hashlib", "sha256"));
			nvps.add(new BasicNameValuePair("timeStep", "30"));
			nvps.add(new BasicNameValuePair("description", "This is a description"));

			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				nvps.add(new BasicNameValuePair("user", user.getEppn()));
			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.debug(responseString);
			    
			    return resultParser.parseInitAuthenticatorTokenResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public LinotpSimpleResponse disableToken(String serial) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/disable");
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("serial", serial));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.debug(responseString);

			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public LinotpSimpleResponse enableToken(String serial) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/enable");
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("serial", serial));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.debug(responseString);

			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public LinotpShowUserResponse getTokenList(UserEntity user) throws TwoFaException {

		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/show");
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				nvps.add(new BasicNameValuePair("user", user.getEppn()));
			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			nvps.add(new BasicNameValuePair("session", adminSession));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.debug(responseString);
			    
			    return resultParser.parseShowUserResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public LinotpSimpleResponse requestAdminSession() throws TwoFaException {
		
		HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/getsession");

		adminSession = null;
		
        try {
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.debug(responseString);
			    
			    CookieStore cookieStore = context.getCookieStore();
			    List<Cookie> cookies = cookieStore.getCookies();
			    for (Cookie cookie : cookies) {
			    	logger.debug("Cookie {}: {}", cookie.getName(), cookie.getValue());
			    	if (cookie.getName().equalsIgnoreCase("admin_session")) {
			    		adminSession = cookie.getValue();
			    	}
			    }

				if (adminSession == null) {
					throw new TwoFaException("LinOTP issued no admin session. Cannot continue.");
				}

			    return resultParser.parseSimpleResponse(responseString);
			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}
	}

	protected List<?> getDataList(Map<?, ?> valueMap) {
        logger.debug("data: " + valueMap.get("data").getClass().toString());
        if (valueMap.get("data") instanceof List<?>) {
        	List<?> dataList = (List<?>) valueMap.get("data");
        	return dataList;
        }
        else {
        	return null;
        }
	}
}
