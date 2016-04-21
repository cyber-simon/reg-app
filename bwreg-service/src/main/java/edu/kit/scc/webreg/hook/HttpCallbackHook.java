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
package edu.kit.scc.webreg.hook;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserServiceHook;

public class HttpCallbackHook implements UserServiceHook {

	private Logger logger = LoggerFactory.getLogger(HttpCallbackHook.class);
	
	private ApplicationConfig appConfig;
	
	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void preUpdateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
	}

	@Override
	public void postUpdateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		
		logger.debug("Calling postUpdateUserFromAttribute for Hook {}", getClass().getName());

		String urlTemplate = appConfig.getConfigValue("HttpCallbackHook_url_template");
		
		if (urlTemplate == null || urlTemplate.equals("")) {
			logger.warn("There is no url template (HttpCallbackHook_url_template) configured");
			return;
		}
		
		String positiveMatchRegex = "(?i)(1|true)";
		if (appConfig.getConfigValue("HttpCallbackHook_positive_match_regex") != null)
			positiveMatchRegex = appConfig.getConfigValue("HttpCallbackHook_positive_match_regex");
		
		Properties p = new Properties();
		p.put("runtime.log.logsystem.log4j.logger", "root");
		VelocityEngine engine = new VelocityEngine(p);
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", user);
		VelocityContext velocityContext = new VelocityContext(context);
		StringWriter out = new StringWriter();

		try {
			engine.evaluate(velocityContext, out, "log", urlTemplate);
		} catch (ParseErrorException e) {
			logger.warn("Velocity problem", e);
			return;
		} catch (MethodInvocationException e) {
			logger.warn("Velocity problem", e);
			return;
		} catch (ResourceNotFoundException e) {
			logger.warn("Velocity problem", e);
			return;
		}

		String url = out.toString();

		user.getGenericStore().put("http_callback_last_call", "" + System.currentTimeMillis());
		user.getGenericStore().remove("http_callback_positive_match");
		
		try {
			RequestConfig config = RequestConfig.custom()
				    .setSocketTimeout(1000)
				    .setConnectTimeout(1000)
				    .build();
			CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build();
			HttpGet httpget = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				try {
					if (entity != null) {
						String r = EntityUtils.toString(entity);
						if (r != null) {
							String lines[] = r.split("\\r?\\n", -1);
							if (lines.length > 0) {
								if (lines[0].matches(positiveMatchRegex)) {
									user.getGenericStore().put("http_callback_positive_match", "" + System.currentTimeMillis());
								}
							}
							
							for (int i=0; i<lines.length; i++) {
								if (user.getGenericStore().containsKey("http_callback_line_" + i)) {
									user.getGenericStore().put("http_callback_line_" + i + "_old", 
											user.getGenericStore().get("http_callback_line_" + i));
								}
								user.getGenericStore().put("http_callback_line_" + i, lines[i].trim());
							}
	
							for (int i=lines.length; i<32; i++) {
								if (user.getGenericStore().containsKey("http_callback_line_" + i + "_old")) {
									user.getGenericStore().remove("http_callback_line_" + i + "_old");
								}
								else {
									break;
								}
							}
							
							for (int i=lines.length; i<32; i++) {
								if (user.getGenericStore().containsKey("http_callback_line_" + i)) {
									user.getGenericStore().put("http_callback_line_" + i + "_old", 
											user.getGenericStore().get("http_callback_line_" + i));
									user.getGenericStore().remove("http_callback_line_" + i);
								}
								else {
									break;
								}
							}
						}
					}
				} catch (ParseException e) {
					logger.warn("Exception", e);
				} catch (IOException e) {
					logger.warn("Exception", e);				
				} finally {
					response.close();
				}
			}
			else {
				logger.warn("Status not ok!");
			}
		} catch (ClientProtocolException e) {
			logger.warn("Exception", e);
		} catch (IOException e) {
			logger.warn("Exception", e);				
		}
	}

	@Override
	public boolean isResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {

		Long negativeCache = 1000L;
		if (appConfig.getConfigValue("HttpCallbackHook_negative_cache_duration") != null)
			negativeCache = Long.parseLong(appConfig.getConfigValue("HttpCallbackHook_negative_cache_duration"));
		
		Long positiveCache = 3600000L;
		if (appConfig.getConfigValue("HttpCallbackHook_positive_cache_duration") != null)
			positiveCache = Long.parseLong(appConfig.getConfigValue("HttpCallbackHook_positive_cache_duration"));
		
		if (user.getGenericStore().containsKey("http_callback_positive_match")) {
			Long positiveMatch = Long.parseLong(user.getGenericStore().get("http_callback_positive_match"));
			if (System.currentTimeMillis() - positiveMatch < positiveCache) {
				logger.debug("HttpCallbackHook skipped due positive Cache for user {}", user.getEppn());
				return false;
			}
		}
		
		if (user.getGenericStore().containsKey("http_callback_last_call")) {
			Long negativeMatch = Long.parseLong(user.getGenericStore().get("http_callback_last_call"));
			if (System.currentTimeMillis() - negativeMatch < negativeCache) {
				logger.debug("HttpCallbackHook skipped due negative Cache for user {}", user.getEppn());
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean isCompleteOverride() {
		return false;
	}

}
