package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("attributeMapHelper")
@ApplicationScoped
public class AttributeMapHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	public String getSingleStringFirst(Map<String, List<Object>> attributeMap, String attributeName) {
		if (! attributeMap.containsKey(attributeName)) {
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
		}
		else {
			return o.toString();
		}
	}
	
	public List<String> attributeListToStringList(Map<String, List<Object>> attributeMap, String attributeName) {
		if (! attributeMap.containsKey(attributeName)) {
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
					sb.append(o.toString());
					sb.append(separator);
				}
			}
		}
		
		if (sb.length() > 0) {
			sb.setLength(sb.length() - separator.length());
		}
		
		return sb.toString();
	}
}
