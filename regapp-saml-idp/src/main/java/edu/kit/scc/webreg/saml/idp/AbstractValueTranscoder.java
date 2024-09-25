package edu.kit.scc.webreg.saml.idp;

import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;

import edu.kit.scc.webreg.service.saml.SamlHelper;

public abstract class AbstractValueTranscoder {

	protected SamlHelper samlHelper;

	public AbstractValueTranscoder(SamlHelper samlHelper) {
		super();
		this.samlHelper = samlHelper;
	}

	protected Attribute buildAttribute(String name, String friendlyName, String nameFormat, String... values) {
		Attribute attribute = samlHelper.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
		attribute.setName(name);
		attribute.setFriendlyName(friendlyName);
		attribute.setNameFormat(nameFormat);

		for (String value : values) {
			XSString xsany = samlHelper.create(XSString.class, XSString.TYPE_NAME, AttributeValue.DEFAULT_ELEMENT_NAME);
			xsany.setValue(value);
			attribute.getAttributeValues().add(xsany);
		}

		return attribute;
	}
}
