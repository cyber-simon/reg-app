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
package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.util.Timeout;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdattr.EntityAttributes;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.AttributeService;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.shibboleth.shared.xml.XMLParserException;
import net.shibboleth.shared.xml.impl.BasicParserPool;

@Named("metadataHelper")
@ApplicationScoped
public class MetadataHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private ApplicationConfig appConfig;
	
	public EntitiesDescriptor fetchMetadata(String url) {
		RequestConfig requestConfig = RequestConfig.custom()
				.setResponseTimeout(getRequestTimeout())
				.setConnectionRequestTimeout(getRequestTimeout())
				.build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
		HttpGet httpGet = new HttpGet(url);
		
		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			logger.info("Fetching Metadata from {}", url);
			try {
				if (response.getCode() == 200) {
					HttpEntity entity = response.getEntity();
					return parseMetadata(entity.getContent());
				}
				else {
					logger.warn("Metadata download from {} got status code {} - bailing out", url, response.getCode());
					return null;
				}
				
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			logger.warn("No Metadata available", e);
			return null;
		} catch (IllegalStateException e) {
			logger.warn("No Metadata available", e);
			return null;
		} catch (IOException e) {
			logger.warn("No Metadata available: {}", e.toString());
			return null;
		}
		finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				logger.info("IOException at httpClient close", e);
			}
		}
	}

	protected EntitiesDescriptor parseMetadata(InputStream inputStream) {
		BasicParserPool basicParserPool = samlHelper.getBasicParserPool();
		UnmarshallerFactory unmarshallerFactory = samlHelper.getUnmarshallerFactory();
		
		try {
			Document document = basicParserPool.parse(new InputStreamReader(inputStream, "UTF-8"));
			Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(document.getDocumentElement());
			XMLObject xmlObject = unmarshaller.unmarshall(document.getDocumentElement());
			EntitiesDescriptor entities = (EntitiesDescriptor) xmlObject;
			
			return entities;
		} catch (XMLParserException e) {
			logger.warn("No Metadata available", e);
			return null;
		} catch (UnmarshallingException e) {
			logger.warn("No Metadata available", e);
			return null;
		} catch (UnsupportedEncodingException e) {
			logger.warn("No UTF-8 support available", e);
			return null;
		}		
	}

	public List<EntityDescriptor> convertEntitiesDescriptor(EntitiesDescriptor entities) {
		List<EntityDescriptor> entityList = new ArrayList<EntityDescriptor>();
		convertEntities(entityList, entities);
		
		return entityList;
	}
	
	public List<EntityDescriptor> filterIdps(List<EntityDescriptor> entities) {
		List<EntityDescriptor> returnList = new ArrayList<EntityDescriptor>();
		
		for (EntityDescriptor entity : entities) {
			IDPSSODescriptor idpsso = entity.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
			if (idpsso != null)
				returnList.add(entity);
		}
		
		return returnList;
	}
	
	public List<EntityDescriptor> filterSps(List<EntityDescriptor> entities) {
		List<EntityDescriptor> returnList = new ArrayList<EntityDescriptor>();
		
		for (EntityDescriptor entity : entities) {
			SPSSODescriptor spsso = entity.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
			if (spsso != null)
				returnList.add(entity);
		}
		
		return returnList;
	}
	
	public List<EntityDescriptor> filterAAs(List<EntityDescriptor> entities) {
		List<EntityDescriptor> returnList = new ArrayList<EntityDescriptor>();
		
		for (EntityDescriptor entity : entities) {
			IDPSSODescriptor idpsso = entity.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
			AttributeAuthorityDescriptor aadesc = entity.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS);
			if (idpsso == null && aadesc != null)
				returnList.add(entity);
		}
		
		return returnList;
	}
	
	public List<EntityDescriptor> filterEntityCategory(List<EntityDescriptor> entities, String category) {
		List<EntityDescriptor> returnList = new ArrayList<EntityDescriptor>();
		
		for (EntityDescriptor entity : entities) {
			List<String> entityCategoryList = getEntityCategoryList(entity);
			
			for (String entityCategory : entityCategoryList) {
				if (category.equals(entityCategory)) {
					returnList.add(entity);
				}
			}
		}
		
		return returnList;
	}
	
	public List<String> getEntityCategoryList(EntityDescriptor entity) {
		List<String> returnList = new ArrayList<String>();
		
		Extensions extensions = entity.getExtensions();
		if (extensions != null) {
			List<XMLObject> extObjs = extensions.getOrderedChildren();
			for (XMLObject xmlObject : extObjs) {
				if (xmlObject instanceof EntityAttributes) {
					EntityAttributes entityAttrs = (EntityAttributes) xmlObject;
					for (Attribute attr : entityAttrs.getAttributes()) {
						if ("http://macedir.org/entity-category".equals(attr.getName())) {
							for (XMLObject value : attr.getAttributeValues()) {
								if (value instanceof XSAny) {
									XSAny any = (XSAny) value;
									if (any.getTextContent() != null) {
										returnList.add(any.getTextContent().trim());
									}
								}
							}
						}
					}
				}
			}
		}
		
		return returnList;
	}
	
	public String getOrganisation(EntityDescriptor entityDesc) {
		if (entityDesc.getOrganization() != null) {
			List<OrganizationDisplayName> displayList = entityDesc.getOrganization().getDisplayNames();
			
			if (displayList.size() > 0)
				return displayList.get(0).getValue();
			else
				return entityDesc.getEntityID();
		}
		else {
			return entityDesc.getEntityID();
		}
	}

	public String getLogo(EntityDescriptor entityDesc) {
		IDPSSODescriptor idpsso = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpsso != null) {
			Extensions extensions = idpsso.getExtensions();
			if (extensions != null) {
				List<XMLObject> uiList = extensions.getUnknownXMLObjects(new QName("urn:oasis:names:tc:SAML:metadata:ui", "UIInfo"));
				if ((uiList.size() == 1) && (uiList.get(0) instanceof UIInfo)) {
					UIInfo uiInfo = (UIInfo) uiList.get(0);
					for (Logo logo : uiInfo.getLogos()) {
						if (logo.getHeight() > 32) {
							return logo.getURI();
						}
					}
				}
			}
		}
		return null;
	}

	public String getLogoSmall(EntityDescriptor entityDesc) {
		IDPSSODescriptor idpsso = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpsso != null) {
			Extensions extensions = idpsso.getExtensions();
			if (extensions != null) {
				List<XMLObject> uiList = extensions.getUnknownXMLObjects(new QName("urn:oasis:names:tc:SAML:metadata:ui", "UIInfo"));
				if ((uiList.size() == 1) && (uiList.get(0) instanceof UIInfo)) {
					UIInfo uiInfo = (UIInfo) uiList.get(0);
					for (Logo logo : uiInfo.getLogos()) {
						if (logo.getHeight() <= 32) {
							return logo.getURI();
						}
					}
				}
			}
		}
		return null;
	}
	
	public Set<SamlIdpScopeEntity> getScopes(EntityDescriptor entityDesc, SamlIdpMetadataEntity idp) {
		Set<SamlIdpScopeEntity> scopeList = new HashSet<SamlIdpScopeEntity>();
		
		IDPSSODescriptor idpsso = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpsso != null) {
			Extensions extensions = idpsso.getExtensions();
			if (extensions != null) {
				List<XMLObject> scopes = extensions.getUnknownXMLObjects(new QName("urn:mace:shibboleth:metadata:1.0", "Scope"));
				for (XMLObject xmlObject : scopes) {
					if (xmlObject instanceof XSAny) {
						XSAny any = (XSAny) xmlObject;
						SamlIdpScopeEntity scope = new SamlIdpScopeEntity();
						scope.setScope(any.getTextContent());
						scope.setRegex(false);
						scope.setIdp(idp);
						scopeList.add(scope);
					}
				}
			}
		}
		
		return scopeList;
	}
	
	public void fillDisplayData(EntityDescriptor entityDesc, SamlMetadataEntity idp) {
		IDPSSODescriptor idpsso = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpsso != null) {
			Extensions extensions = idpsso.getExtensions();
			if (extensions != null) {
				List<XMLObject> uiInfoList = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
				if (uiInfoList.size() > 0) {
					XMLObject xmlObject = uiInfoList.get(0);
					if (xmlObject instanceof UIInfo) {
						UIInfo uiInfo = (UIInfo) xmlObject;
	
						if (uiInfo.getDescriptions().size() > 0) {
							idp.setDescription(uiInfo.getDescriptions().get(0).getValue());
						}
						if (uiInfo.getDisplayNames().size() > 0) {
							idp.setDisplayName(uiInfo.getDisplayNames().get(0).getValue());
						}
						if (uiInfo.getInformationURLs().size() > 0) {
							idp.setInformationUrl(uiInfo.getInformationURLs().get(0).getURI());
						}
					}
				}
			}
		}
	}
	
	public void fillDisplayData(EntityDescriptor entityDesc, SamlSpMetadataEntity sp) {
		SPSSODescriptor spsso = entityDesc.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (spsso != null) {
			Extensions extensions = spsso.getExtensions();
			if (extensions != null) {
				List<XMLObject> uiInfoList = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
				if (uiInfoList.size() > 0) {
					XMLObject xmlObject = uiInfoList.get(0);
					if (xmlObject instanceof UIInfo) {
						UIInfo uiInfo = (UIInfo) xmlObject;
	
						if (uiInfo.getDescriptions().size() > 0) {
							sp.setDescription(uiInfo.getDescriptions().get(0).getValue());
						}
						if (uiInfo.getDisplayNames().size() > 0) {
							sp.setDisplayName(uiInfo.getDisplayNames().get(0).getValue());
						}
						if (uiInfo.getInformationURLs().size() > 0) {
							sp.setInformationUrl(uiInfo.getInformationURLs().get(0).getURI());
						}
					}
				}
			}
		}
	}
	
	public SingleSignOnService getSSO(EntityDescriptor entityDesc, String binding) {
		IDPSSODescriptor idpSsoDesc = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpSsoDesc != null) {
			List<SingleSignOnService> ssos = idpSsoDesc.getSingleSignOnServices();
			for (SingleSignOnService sso : ssos) {
				if (sso.getBinding().equals(binding)) {
					return sso;
				}
			}
		}
		return null;
	}
	
	public SingleLogoutService getSLO(EntityDescriptor entityDesc, String binding) {
		IDPSSODescriptor idpSsoDesc = entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (idpSsoDesc != null) {
			List<SingleLogoutService> slos = idpSsoDesc.getSingleLogoutServices();
			for (SingleLogoutService slo : slos) {
				if (slo.getBinding().equals(binding)) {
					return slo;
				}
			}
		}
		return null;
	}
	
	public AttributeService getAttributeService(EntityDescriptor entityDesc) {
		AttributeAuthorityDescriptor idpAtrDesc = entityDesc.getAttributeAuthorityDescriptor(SAMLConstants.SAML20P_NS);
		if (idpAtrDesc != null) {
			List<AttributeService> attrs = idpAtrDesc.getAttributeServices();
			for (AttributeService attr : attrs) {
				if (attr.getBinding().equals(SAMLConstants.SAML2_SOAP11_BINDING_URI)) {
					return attr;
				}
			}
		}
		return null;
	}
	
	private void convertEntities(List<EntityDescriptor> entityList, EntitiesDescriptor entities) {
		for (EntityDescriptor entity : entities.getEntityDescriptors()) {
			entityList.add(entity);
		}

		for (EntitiesDescriptor entitiesInEntities : entities.getEntitiesDescriptors()) {
			convertEntities(entityList, entitiesInEntities);
		}		
	}
	
	private Timeout getRequestTimeout() {
		String aqString = appConfig.getConfigValue("metadata_timeout");
		if (aqString == null)
			return Timeout.ofSeconds(30);
		else 
			return Timeout.ofMilliseconds(Integer.parseInt(aqString));
	}
}
