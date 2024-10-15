package edu.kit.scc.webreg.saml.idp;

import org.opensaml.saml.saml2.core.Attribute;

import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.service.saml.SamlHelper;

public class SingleStringValueTranscoder extends AbstractValueTranscoder implements ValueTranscoder {

	private String outgoingName;
	private String outgoingFriendlyName;
	private String nameFormat;

	public SingleStringValueTranscoder(SamlHelper samlHelper, String outgoingName, String outgoingFriendlyName, String nameFormat) {
		super(samlHelper);
		this.outgoingName = outgoingName;
		this.outgoingFriendlyName = outgoingFriendlyName;
		this.nameFormat = nameFormat;
	}
	
	public Attribute transcode(ValueEntity value) {
		if (value instanceof StringValueEntity)
			return buildAttribute(outgoingName, outgoingFriendlyName, nameFormat,
				((StringValueEntity) value).getValueString());
		else if (value instanceof StringListValueEntity) 
			return buildAttribute(outgoingName, outgoingFriendlyName, nameFormat,
					((StringListValueEntity) value).getValueList().toArray(new String[] {}));
		else 
			throw new IllegalArgumentException("Cannot transcaode value of type " + value.getClass().getSimpleName());
	}	
}
