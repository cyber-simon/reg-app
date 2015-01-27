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

import javax.servlet.http.HttpServletResponse;

import org.opensaml.ws.message.encoder.MessageEncodingException;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface Saml2RedirectService {

	public void redirectClient(SamlIdpMetadataEntity idpEntity,
			SamlSpConfigurationEntity spEntity, HttpServletResponse response)
			throws MessageEncodingException;

}
