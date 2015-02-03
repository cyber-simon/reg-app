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

import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;

public interface Saml2ResponseValidationService {

	public void verifyIssuer(SamlMetadataEntity idpEntity, Response samlResponse)
			throws SamlAuthenticationException;

	void verifyExpiration(Response samlResponse, Long expiryMillis)
			throws SamlAuthenticationException;

	void verifyStatus(Response samlResponse) throws SamlAuthenticationException;

	void validateSignature(SignableSAMLObject assertion,
			Issuer issuer, EntityDescriptor idpEntityDescriptor) throws SamlAuthenticationException;
	
}
