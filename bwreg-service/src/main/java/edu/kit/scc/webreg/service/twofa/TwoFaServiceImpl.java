package edu.kit.scc.webreg.service.twofa;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.UserEntity;

@Stateless
public class TwoFaServiceImpl implements TwoFaService {

	@Inject
	private Logger logger;
	
	@Inject
	private TwoFaConfigurationResolver configResolver;
	
	@Inject
	private UserDao userDao;
	
	@Override
	public void findByUserId(Long userId) throws TwoFaException {
		UserEntity user = userDao.findById(userId);
		
		Map<String, String> configMap = configResolver.resolveConfig(user);

		try {
			URI uri = new URI(configMap.get("url"));
			HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
			AuthCache authCache = new BasicAuthCache();
			authCache.put(targetHost, new BasicScheme());

			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(configMap.get("username"), configMap.get("password"));
			credsProvider.setCredentials(AuthScope.ANY, credentials);

			HttpClientContext context = HttpClientContext.create();
			context.setCredentialsProvider(credsProvider);
			context.setAuthCache(authCache);

			RequestConfig config = RequestConfig.custom()
				    .setSocketTimeout(5000)
				    .setConnectTimeout(5000)
				    .build();
			CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
			String adminSession = null;
			
			try {
				
				HttpPost httpPost = new HttpPost(configMap.get("url") + "/admin/getsession");

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
				} finally {
					response.close();
				}
				
				if (adminSession == null) {
					throw new TwoFaException("LinOTP issued no admin session. Cannot continue.");
				}

				httpPost = new HttpPost(configMap.get("url") + "/admin/show");
				List<NameValuePair> nvps = new ArrayList <NameValuePair>();
		        if (configMap.containsKey("userId"))
			        nvps.add(new BasicNameValuePair("user", configMap.get("userId")));
		        else
		        	nvps.add(new BasicNameValuePair("user", user.getEppn()));
		        if (configMap.containsKey("realm"))
		        	nvps.add(new BasicNameValuePair("realm", configMap.get("realm")));
		        nvps.add(new BasicNameValuePair("session", adminSession));
		        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		        
		        response = httpClient.execute(targetHost, httpPost, context);
				try {
			        HttpEntity entity = response.getEntity();
		            String responseString = EntityUtils.toString(entity);
		            logger.debug(responseString);
		            ObjectMapper om = new ObjectMapper();
					@SuppressWarnings("unchecked")
					Map<String, Object> map = om.readValue(responseString, Map.class);
					if (map.get("result") instanceof Map<?, ?>) {
						Map<?, ?> resultMap = (Map<?, ?>) map.get("result");
			            logger.debug("value: " + resultMap.get("value").getClass().toString());
						if (resultMap.get("value") instanceof Map<?, ?>) {
							Map<?, ?> valueMap = (Map<?, ?>) resultMap.get("value");
				            logger.debug("data: " + valueMap.get("data").getClass().toString());
				            if (valueMap.get("data") instanceof List<?>) {
				            	List<?> dataList = (List<?>) valueMap.get("data");
				            	for (Object data : dataList) {
				            		if (data instanceof Map<?, ?>) {
				            			Map<?, ?> dataMap = (Map<?, ?>) data;
					            		logger.debug("ID: " + dataMap.get("LinOtp.TokenId"));
				            		}
				            	}
				            }
						}
					}
				} finally {
					response.close();
				}
			} finally {
				httpClient.close();
			}
		} catch (IOException | URISyntaxException e) {
			throw new TwoFaException(e);
		}
	}
	
}
