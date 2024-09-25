package edu.kit.scc.webreg.saml.idp;

import org.opensaml.saml.saml2.core.Attribute;

import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public interface ValueTranscoder {
	Attribute transcode(ValueEntity value);
}
