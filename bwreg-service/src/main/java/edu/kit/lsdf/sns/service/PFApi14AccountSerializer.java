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
package edu.kit.lsdf.sns.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PFApi14AccountSerializer {

	public PFAccount unmarshal(Map<String, Object> userData) {
		Map<String, Object> rs = (Map<String, Object>) userData.get("ResultSet");
		List<Object> list = (List<Object>) rs.get("Result");
		Map<String, Object> result = (Map<String, Object>) list.get(0);
		PFAccount account = new PFAccount();
		account.setId(convertValue(result.get("ID")));
		account.setUsername(convertValue(result.get("username")));
		account.setSpaceAllowed(convertValue(result.get("spaceAllowed")));
		account.setFirstname(convertValue(result.get("firstname")));
		account.setSurname(convertValue(result.get("surname")));
		account.setCustom1(convertValue(result.get("custom1")));
		account.setCustom2(convertValue(result.get("custom2")));
		account.setCustom3(convertValue(result.get("custom3")));
		account.setPassword(convertValue(result.get("password")));
		account.setValidTil(convertValue(result.get("validTill")));
		account.setNotes(convertValue(result.get("notes")));
		account.setEmail(convertValue(result.get("emails")));

		return account;
	}
	
	public Map<String, String> marshal(PFAccount account) {
		Map<String, String> parameterMap = new HashMap<String, String>();
		
		parameterMap.put("id", account.getId());
		putIfNotNull(parameterMap, "name", account.getUsername());
		putIfNotNull(parameterMap, "spaceAllowed", account.getSpaceAllowed());
		putIfNotNull(parameterMap, "firstname", account.getFirstname());
		putIfNotNull(parameterMap, "surname", account.getSurname());
		putIfNotNull(parameterMap, "custom1", account.getCustom1());
		putIfNotNull(parameterMap, "custom2", account.getCustom2());
		putIfNotNull(parameterMap, "custom3", account.getCustom3());
		putIfNotNull(parameterMap, "password", account.getPassword());
		putIfNotNull(parameterMap, "validTill", account.getValidTil());
		putIfNotNull(parameterMap, "notes", account.getNotes());
		putIfNotNull(parameterMap, "emails", account.getEmail());

		return parameterMap;
	}
	
	private void putIfNotNull(Map<String, String> parameterMap, String key, String value) {
		if (value != null)
			parameterMap.put(key, value);
	}
	
	private String convertValue(Object value) {
		if (value != null)
			return value.toString();
		else
			return null;
	}
}
