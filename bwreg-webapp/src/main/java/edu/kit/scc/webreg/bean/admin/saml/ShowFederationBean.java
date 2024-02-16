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
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.FederationService;
import edu.kit.scc.webreg.service.SamlAAMetadataService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.disco.FederationUpdateService;

@Named
@ViewScoped
public class ShowFederationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationService service;

	@Inject
	private FederationUpdateService federationUpdateService;
	
	@Inject
	private SamlIdpMetadataService idpService;
	
	@Inject
	private SamlSpMetadataService spService;
	
	@Inject
	private SamlAAMetadataService aaService;
	
	private FederationEntity entity;
	
	private List<SamlIdpMetadataEntity> idpList;
	private List<SamlSpMetadataEntity> spList;
	private List<SamlAAMetadataEntity> aaList;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findWithIdpEntities(id);
		}
		idpList = idpService.findAllByFederation(entity);
		spList = spService.findAllByFederation(entity);
		aaList = aaService.findAllByFederation(entity);
	}
	
	public void poll() {
		federationUpdateService.updateFederation(entity);
	}
	
	public FederationEntity getEntity() {
		return entity;
	}

	public void setEntity(FederationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<SamlIdpMetadataEntity> getIdpList() {
		return idpList;
	}

	public void setIdpList(List<SamlIdpMetadataEntity> idpList) {
		this.idpList = idpList;
	}

	public List<SamlSpMetadataEntity> getSpList() {
		return spList;
	}

	public List<SamlAAMetadataEntity> getAaList() {
		return aaList;
	}
}
