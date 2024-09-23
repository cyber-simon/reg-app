package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.Serializable;

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public interface SamlAttributeQueryService extends Serializable {

	void consumeAttributeQuery(HttpServletRequest request, HttpServletResponse response,
			SamlAAConfigurationEntity aaConfig) throws IOException;
}
