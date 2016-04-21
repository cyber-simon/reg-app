package edu.kit.scc.webreg.service.saml;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.w3c.dom.ls.LSSerializerFilter;

@Named("xmlHelper")
@ApplicationScoped
public class XMLHelper {

	public String nodeToString(Node node) {
		StringWriter writer = new StringWriter();
		writeNode(node, writer);
		return writer.toString();
	}

	public String prettyPrintXML(Node node) {
		StringWriter writer = new StringWriter();
		writeNode(node, writer, getPrettyPrintParams());
		return writer.toString();
	}

	private Map<String, Object> getPrettyPrintParams() {
		Map<String, Object> prettyPrintParams = new HashMap<String, Object>();
		prettyPrintParams.put("format-pretty-print", Boolean.TRUE);
		return prettyPrintParams;
	}

	public void writeNode(Node node, Writer output) {
		writeNode(node, output, null);
	}

	public void writeNode(Node node, Writer output,
			Map<String, Object> serializerParams) {
		DOMImplementationLS domImplLS = getLSDOMImpl(node);

		LSSerializer serializer = getLSSerializer(domImplLS, serializerParams);

		LSOutput serializerOut = domImplLS.createLSOutput();
		serializerOut.setCharacterStream(output);

		serializer.write(node, serializerOut);
	}

	public DOMImplementationLS getLSDOMImpl(Node node) {
		DOMImplementation domImpl;
		if (node instanceof Document) {
			domImpl = ((Document) node).getImplementation();
		} else {
			domImpl = node.getOwnerDocument().getImplementation();
		}

		DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl
				.getFeature("LS", "3.0");
		return domImplLS;
	}

	public LSSerializer getLSSerializer(DOMImplementationLS domImplLS,
			Map<String, Object> serializerParams) {
		LSSerializer serializer = domImplLS.createLSSerializer();

		serializer.setFilter(new LSSerializerFilter() {

			public short acceptNode(Node arg0) {
				return FILTER_ACCEPT;
			}

			public int getWhatToShow() {
				return SHOW_ALL;
			}
		});

		if (serializerParams != null) {
			DOMConfiguration serializerDOMConfig = serializer.getDomConfig();
			for (String key : serializerParams.keySet()) {
				serializerDOMConfig
						.setParameter(key, serializerParams.get(key));
			}
		}

		return serializer;
	}
}
