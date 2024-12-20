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
package edu.kit.scc.webreg.bean.admin.saml;

import java.io.Serializable;

import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.shibboleth.shared.xml.XMLParserException;

@Named
@ViewScoped
public class EditSamlSpBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlSpMetadataService service;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private MetadataHelper metadataHelper;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private SamlSpMetadataEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
		}
	}
	
	public String save() {
		try {
			EntityDescriptor entityDescriptor = samlHelper.unmarshalThrow(entity.getEntityDescriptor(), EntityDescriptor.class);
			if (entityDescriptor.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).size() < 1) {
				messageGenerator.addErrorMessage("No SPSSODescriptor element");
				return null;
			}
			
			entity.setEntityId(entityDescriptor.getEntityID());
			entity.setEntityDescriptor(samlHelper.marshal(entityDescriptor));
			entity.setOrgName(metadataHelper.getOrganisation(entityDescriptor));
			metadataHelper.fillDisplayData(entityDescriptor, entity);

			entity = service.save(entity);
			return "show-sp.xhtml?faces-redirect=true&id=" + entity.getId();
		} catch (XMLParserException | UnmarshallingException e) {
			messageGenerator.addErrorMessage("Parsing error", e.getMessage());
			return null;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SamlSpMetadataEntity getEntity() {
		return entity;
	}

	public void setEntity(SamlSpMetadataEntity entity) {
		this.entity = entity;
	}
}
