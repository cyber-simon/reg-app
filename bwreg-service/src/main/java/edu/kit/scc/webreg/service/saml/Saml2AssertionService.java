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
import java.util.List;
import java.util.Map;

import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.EncryptedID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.encryption.DecryptionException;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;

public interface Saml2AssertionService {

	public Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, String privateKey)
			throws IOException, DecryptionException, SamlAuthenticationException;

	Map<String, List<Object>> extractAttributes(Assertion assertion);

	SAMLObject decryptNameID(EncryptedID encryptedID, String privateKey)
			throws IOException, DecryptionException, SamlAuthenticationException;

	Assertion processSamlResponse(Response samlResponse,
			SamlMetadataEntity idpEntity,
			EntityDescriptor idpEntityDescriptor,
			SamlSpConfigurationEntity spEntity) throws IOException, DecryptionException, SamlAuthenticationException;

	String extractPersistentId(Assertion assertion,
			SamlSpConfigurationEntity spEntity) throws IOException, DecryptionException, SamlAuthenticationException;

	Assertion processSamlResponse(Response samlResponse,
			SamlMetadataEntity idpEntity,
			EntityDescriptor idpEntityDescriptor,
			SamlSpConfigurationEntity spEntity, boolean checkSignature)
			throws IOException, DecryptionException, SamlAuthenticationException;

}
