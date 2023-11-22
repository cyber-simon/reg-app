package edu.kit.scc.webreg.as;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.audit.AttributeSourceAuditor;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceQueryStatus;
import edu.kit.scc.webreg.exc.PropertyReaderException;
import edu.kit.scc.webreg.exc.UserUpdateException;

public class HttpUrlSingleAttributeSource extends
		AbstractAttributeSourceWorkflow {

	private static final long serialVersionUID = 1L;

	@Override
	public Boolean pollUserAttributes(ASUserAttrEntity asUserAttr, ASUserAttrValueDao asValueDao,
			GroupDao groupDao, AttributeSourceAuditor auditor) throws UserUpdateException {
		
		Boolean changed = false;
		
		init(asUserAttr, asValueDao, groupDao, auditor);
		
		String urlTemplate;
		try {
			urlTemplate = prop.readProp("url_template");
		} catch (PropertyReaderException e1) {
			throw new UserUpdateException(e1);
		}

		UserEntity user = asUserAttr.getUser();

		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.init();
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("user", user);
		VelocityContext velocityContext = new VelocityContext(context);
		StringWriter out = new StringWriter();

		try {
			engine.evaluate(velocityContext, out, "log", urlTemplate);
		} catch (ParseErrorException e) {
			logger.warn("Velocity problem", e);
			asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
			asUserAttr.setQueryMessage(e.getMessage());
			return changed;
		} catch (MethodInvocationException e) {
			logger.warn("Velocity problem", e);
			asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
			asUserAttr.setQueryMessage(e.getMessage());
			return changed;
		} catch (ResourceNotFoundException e) {
			logger.warn("Velocity problem", e);
			asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
			asUserAttr.setQueryMessage(e.getMessage());
			return changed;
		}

		String url = out.toString();

		RequestConfig config = RequestConfig.custom()
			    .setConnectTimeout(Timeout.ofSeconds(5))
			    .build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build();
		HttpGet httpget = new HttpGet(url);
		CloseableHttpResponse response;
		try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			logger.info("Problem", e);
			asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
			asUserAttr.setQueryMessage(e.getMessage());
			return changed;
		} catch (IOException e) {
			logger.info("Problem", e);
			asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
			asUserAttr.setQueryMessage(e.getMessage());
			return changed;
		}
		HttpEntity entity = response.getEntity();

		if (response.getCode() == HttpStatus.SC_OK) {
			if (entity != null) {
				try {
					String r = EntityUtils.toString(entity);
					ObjectMapper om = new ObjectMapper();
					
					try {
						@SuppressWarnings("unchecked")
						Map<String, Object> map = om.readValue(r, Map.class);

						logger.debug("Got {} values", map.size());
						changed |= createOrUpdateValues(map);
						
						asUserAttr.setQueryStatus(AttributeSourceQueryStatus.SUCCESS);
						
					} catch (JsonMappingException e) {
						/*
						 * Datasource generates invalid JSON
						 */
						logger.warn("Json Parse failed: {}", e.getMessage());
						asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
						if (e.getMessage().length() > 510)
							asUserAttr.setQueryMessage(e.getMessage().substring(0, 510));
						else
							asUserAttr.setQueryMessage(e.getMessage());
					}
				} catch (ParseException e) {
					logger.warn("Parse failed: {}", e.getMessage());
					asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
					if (e.getMessage().length() > 510)
						asUserAttr.setQueryMessage(e.getMessage().substring(0, 510));
					else
						asUserAttr.setQueryMessage(e.getMessage());
				} catch (IOException e) {
					logger.warn("IOException with Attribute source: {}", e.getMessage());
					asUserAttr.setQueryStatus(AttributeSourceQueryStatus.FAIL);
					if (e.getMessage().length() > 510)
						asUserAttr.setQueryMessage(e.getMessage().substring(0, 510));
					else
						asUserAttr.setQueryMessage(e.getMessage());
				}
			}
		}
		else {
			/*
			 * probably no info for this user from datasource
			 */
			logger.debug("Status HttpUrlSingleAS is not OK. It is {} - {}", response.getCode(), response.getReasonPhrase());
			asUserAttr.setQueryStatus(AttributeSourceQueryStatus.USER_NOT_FOUND);
			asUserAttr.setQueryMessage("Status HttpUrlSingleAS is " + response.getCode());
		}
		
		return changed;
	}

}
