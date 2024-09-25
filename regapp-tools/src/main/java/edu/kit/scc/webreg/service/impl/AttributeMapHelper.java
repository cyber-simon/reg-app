package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import net.minidev.json.JSONAware;

@Named("attributeMapHelper")
@ApplicationScoped
public class AttributeMapHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	public String getSingleStringFirst(Map<String, List<Object>> attributeMap, String attributeName) {
		if (!attributeMap.containsKey(attributeName)) {
			return null;
		}

		List<Object> objList = attributeMap.get(attributeName);

		return getSingleStringFirst(objList);
	}

	public String getSingleStringFirst(List<Object> objList) {
		if (objList == null || objList.size() < 1) {
			return null;
		}

		Object o = objList.get(0);
		if (o instanceof String) {
			return (String) o;
		} else {
			if (o == null)
				return null;
			else
				return o.toString();
		}
	}

	public List<String> attributeListToStringList(Map<String, List<Object>> attributeMap, String attributeName) {
		if (!attributeMap.containsKey(attributeName)) {
			return null;
		}

		List<Object> objList = attributeMap.get(attributeName);

		return attributeListToStringList(objList);
	}

	public List<String> attributeListToStringList(List<Object> attributeList) {
		List<String> retList = new ArrayList<String>(attributeList.size());
		if (attributeList != null) {
			for (Object o : attributeList) {
				if (o != null) {
					retList.add(o.toString());
				}
			}
		}
		return retList;
	}

	public String attributeListToString(List<Object> attributeList) {
		return attributeListToString(attributeList, ";");
	}

	public String attributeListToString(List<Object> attributeList, String separator) {
		StringBuffer sb = new StringBuffer();

		if (attributeList != null) {
			for (Object o : attributeList) {
				if (o != null) {
					if (o instanceof JSONAware) {
						sb.append(((JSONAware) o).toJSONString());
						sb.append(separator);
					} else {
						sb.append(o.toString());
						sb.append(separator);
					}
				}
			}
		}

		if (sb.length() > 0) {
			sb.setLength(sb.length() - separator.length());
		}

		return sb.toString();
	}

	public void convertAttributeNames(Set<Entry<String, List<Object>>> entrySet,
			List<String> printableAttributesList, Map<String, String> printableAttributesMap,
			Map<String, String> unprintableAttributesMap) {
		for (Entry<String, List<Object>> entry : entrySet) {
			if (entry.getKey().equals("urn:oid:0.9.2342.19200300.100.1.3")) {
				printableAttributesList.add("email");
				printableAttributesMap.put("email", attributeListToString(entry.getValue(), ","));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.6")) {
				printableAttributesList.add("eppn");
				printableAttributesMap.put("eppn", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:2.5.4.42")) {
				printableAttributesList.add("given_name");
				printableAttributesMap.put("given_name", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:2.5.4.4")) {
				printableAttributesList.add("sur_name");
				printableAttributesMap.put("sur_name", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.1.1.1.1")) {
				printableAttributesList.add("gid_number");
				printableAttributesMap.put("gid_number", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("http://bwidm.de/bwidmCC")) {
				printableAttributesList.add("primary_group");
				printableAttributesMap.put("primary_group", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("http://bwidm.de/bwidmOrgId")) {
				printableAttributesList.add("bwidm_org_id");
				printableAttributesMap.put("bwidm_org_id", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("memberOf")) {
				printableAttributesList.add("groups");
				printableAttributesMap.put("groups", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("http://bwidm.de/bwidmMemberOf")) {
				printableAttributesList.add("groups");
				printableAttributesMap.put("groups", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.7")) {
				printableAttributesList.add("entitlement");
				printableAttributesMap.put("entitlement", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:0.9.2342.19200300.100.1.1")) {
				printableAttributesList.add("uid");
				printableAttributesMap.put("uid", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.1.1.1.0")) {
				printableAttributesList.add("uid_number");
				printableAttributesMap.put("uid_number", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.9")) {
				printableAttributesList.add("affiliation");
				printableAttributesMap.put("affiliation", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.13")) {
				printableAttributesList.add("epuid");
				printableAttributesMap.put("epuid", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oasis:names:tc:SAML:attribute:pairwise-id")) {
				printableAttributesList.add("pairwise_id");
				printableAttributesMap.put("pairwise_id", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oasis:names:tc:SAML:attribute:subject-id")) {
				printableAttributesList.add("subject_id");
				printableAttributesMap.put("subject_id", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.1")) {
				printableAttributesList.add("bwcard_number");
				printableAttributesMap.put("bwcard_number", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.2")) {
				printableAttributesList.add("bwcard_chip_id");
				printableAttributesMap.put("bwcard_chip_id", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.3")) {
				printableAttributesList.add("bwcard_escn");
				printableAttributesMap.put("bwcard_escn", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.57378.1.4")) {
				printableAttributesList.add("bwcard_valid_to");
				printableAttributesMap.put("bwcard_valid_to", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.149")) {
				printableAttributesList.add("attributes.bundid.unknown");
				printableAttributesMap.put("attributes.bundid.unknown", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.261.94")) {
				printableAttributesList.add("attributes.bundid.assurance");
				printableAttributesMap.put("attributes.bundid.assurance",
						attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:2.5.4.16")) {
				printableAttributesList.add("attributes.postal_address");
				printableAttributesMap.put("attributes.postal_address", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:2.5.4.17")) {
				printableAttributesList.add("attributes.postal_code");
				printableAttributesMap.put("attributes.postal_code", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:2.5.4.18")) {
				printableAttributesList.add("attributes.bundid.postal_handle");
				printableAttributesMap.put("attributes.bundid.postal_handle",
						attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.55")) {
				printableAttributesList.add("attributes.birthdate");
				printableAttributesMap.put("attributes.birthdate", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.5.5.7.9.2")) {
				printableAttributesList.add("attributes.place_of_birth");
				printableAttributesMap.put("attributes.place_of_birth", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:0.9.2342.19200300.100.1.40")) {
				printableAttributesList.add("attributes.personal_title");
				printableAttributesMap.put("attributes.personal_title", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.225599")) {
				printableAttributesList.add("attributes.bundid.nationality");
				printableAttributesMap.put("attributes.bundid.nationality",
						attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.2.40.0.10.2.1.1.225566")) {
				printableAttributesList.add("attributes.birth_name");
				printableAttributesMap.put("attributes.birth_name", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25484.494450.3")) {
				printableAttributesList.add("attributes.bundid.bpk2");
				printableAttributesMap.put("attributes.bundid.bpk2", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25484.494450.2")) {
				printableAttributesList.add("attributes.bundid.assertion_proved_by");
				printableAttributesMap.put("attributes.bundid.assertion_proved_by",
						attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25484.494450.1")) {
				printableAttributesList.add("attributes.bundid.assertion_valid_until");
				printableAttributesMap.put("attributes.bundid.assertion_valid_until",
						attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.33592.1.3.5")) {
				printableAttributesList.add("attributes.bundid.gender");
				printableAttributesMap.put("attributes.bundid.gender", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:2.5.4.7")) {
				printableAttributesList.add("attributes.locality_name");
				printableAttributesMap.put("attributes.locality_name", attributeListToString(entry.getValue(), ", "));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25178.1.2.9")) {
				printableAttributesList.add("attributes.schac_home_organization");
				printableAttributesMap.put("attributes.schac_home_organization", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.25178.1.2.14")) {
				printableAttributesList.add("attributes.schac_personal_unique_code");
				printableAttributesMap.put("attributes.schac_personal_unique_code", getSingleStringFirst(entry.getValue()));
			} else if (entry.getKey().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.11")) {
				printableAttributesList.add("attributes.edu_person_assurance");
				printableAttributesMap.put("attributes.edu_person_assurance", attributeListToString(entry.getValue(), ", "));
			} else {
				unprintableAttributesMap.put(entry.getKey(), attributeListToString(entry.getValue(), ", "));
			}
		}
	}
}
