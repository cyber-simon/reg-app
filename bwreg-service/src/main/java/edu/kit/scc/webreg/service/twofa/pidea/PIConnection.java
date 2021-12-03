package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.service.twofa.TwoFaException;

public class PIConnection {

	private static final Logger logger = LoggerFactory.getLogger(PIConnection.class);
	
	private Map<String, String> configMap;

	private PIResultParser resultParser;
	
	private URI uri;
	private HttpHost targetHost;
	private RequestConfig config;
	private CloseableHttpClient httpClient;
	private HttpClientContext context;
	
	private String adminUsername, adminPassword, adminSession;
	
	public PIConnection(Map<String, String> configMap) throws TwoFaException {
		super();
		this.configMap = configMap;
		resultParser = new PIResultParser();
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

		context = HttpClientContext.create();

		config = RequestConfig.custom()
			    .setSocketTimeout(30000)
			    .setConnectTimeout(30000)
			    .build();
		httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

		adminUsername = configMap.get("username");
		adminPassword = configMap.get("password");
	}

	public PISimpleResponse checkToken(String token) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/validate/check");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();

			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				throw new TwoFaException("userId missing in config map");
			
			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
			nvps.add(new BasicNameValuePair("pass", token));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}
	}
	
	public PISimpleResponse checkSpecificToken(String serial, String token) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/validate/check_s");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();

		    if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
		    nvps.add(new BasicNameValuePair("serial", serial));
			nvps.add(new BasicNameValuePair("pass", token));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}
	}
	
	public PIInitAuthenticatorTokenResponse createAuthenticatorToken() throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/init");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("type", "totp"));
			nvps.add(new BasicNameValuePair("otplen", "6"));
			nvps.add(new BasicNameValuePair("genkey", "1"));
			nvps.add(new BasicNameValuePair("hashlib", "sha1"));
			nvps.add(new BasicNameValuePair("timeStep", "30"));
			nvps.add(new BasicNameValuePair("description", "INIT,DELABLE,BWIDM,TS " + formatDate() + ","));

			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				throw new TwoFaException("userId missing in config map");

			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseInitAuthenticatorTokenResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PIInitAuthenticatorTokenResponse createYubicoToken(String yubi) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/init");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("type", "yubico"));
			nvps.add(new BasicNameValuePair("yubico.tokenid", yubi));
			nvps.add(new BasicNameValuePair("description", "This is a description"));

			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				throw new TwoFaException("userId missing in config map");

			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseInitAuthenticatorTokenResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public PIInitAuthenticatorTokenResponse createBackupTanList() throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/init");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("type", "hmac"));
			nvps.add(new BasicNameValuePair("otplen", "8"));
			nvps.add(new BasicNameValuePair("genkey", "1"));
			nvps.add(new BasicNameValuePair("hashlib", "sha1"));
			nvps.add(new BasicNameValuePair("description", "INIT,DELABLE,BWIDM,TS " + formatDate() + ","));

			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				throw new TwoFaException("userId missing in config map");

			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseInitAuthenticatorTokenResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public PIGetBackupTanListResponse getBackupTanList(String serial, int count) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/gettoken/getmultiotp");
			
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			nvps.add(new BasicNameValuePair("serial", serial));
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("count", "" + count));

			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseGetBackupTanListResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PISetFieldResult initToken(String serial) throws TwoFaException {
		return setTokenField(serial, "description", "ACTIVE,DELABLE,TS " + formatDate() + ",");
	}
	
	public PISetFieldResult setTokenField(String serial, String key, String value) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/set");
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
			nvps.add(new BasicNameValuePair("session", adminSession));
			nvps.add(new BasicNameValuePair("serial", serial));
			nvps.add(new BasicNameValuePair(key, value));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);

			    return resultParser.parseSetFieldResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PISimpleResponse disableToken(String serial) throws TwoFaException {
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
			    logger.trace(responseString);

			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PISimpleResponse enableToken(String serial) throws TwoFaException {
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
			    logger.trace(responseString);

			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PISimpleResponse deleteToken(String serial) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/remove");
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
			    logger.trace(responseString);

			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public PISimpleResponse resetFailcounter(String serial) throws TwoFaException {
		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/reset");
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
			    logger.trace(responseString);

			    return resultParser.parseSimpleResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PIShowUserResponse getTokenList() throws TwoFaException {

		try {
			HttpPost httpPost = new HttpPost(configMap.get("url") + "/token/");
			httpPost.addHeader("Authorization", adminSession);
			List<NameValuePair> nvps = new ArrayList <NameValuePair>();
			if (configMap.containsKey("userId"))
			    nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
			else
				throw new TwoFaException("userId missing in config map");

			if (configMap.containsKey("realm"))
				nvps.add(new BasicNameValuePair("tokenrealm", configMap.get("realm")));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			
			CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    return resultParser.parseShowUserResponse(responseString);

			} finally {
				response.close();
			}
		} catch (ParseException | IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PIAuthResponse requestAdminSession() throws TwoFaException {
        try {
    		HttpPost httpPost = new HttpPost(configMap.get("url") + "/auth");

    		List<NameValuePair> nvps = new ArrayList <NameValuePair>();
    		nvps.add(new BasicNameValuePair("username", adminUsername));
    		nvps.add(new BasicNameValuePair("password", adminPassword));
    		httpPost.setEntity(new UrlEncodedFormEntity(nvps));

    		adminSession = null;

    		CloseableHttpResponse response = httpClient.execute(targetHost, httpPost, context);
			
			try {
			    HttpEntity entity = response.getEntity();
			    String responseString = EntityUtils.toString(entity);
			    logger.trace(responseString);
			    
			    PIAuthResponse authResponse = resultParser.parseAuthResponse(responseString);
			    adminSession = authResponse.getResult().getValue().getToken();
			    return authResponse;
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
	
	protected String formatDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		return formatter.format(new Date());
	}
}
