package edu.kit.scc.webreg.service.oidc.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

@ApplicationScoped
public class OidcTokenHelper {

	public Map<String, List<Object>> convertToAttributeMap(IDTokenClaimsSet claims, UserInfo userInfo, RefreshToken refreshToken, BearerAccessToken bat) {
		Map<String, List<Object>> attributeMap = new HashMap<String, List<Object>>();
		
		List<Object> tempList;
		
		if (claims != null) {
			tempList = new ArrayList<Object>();
			tempList.add(claims);
			attributeMap.put("claims", tempList);
		}

		if (userInfo != null) {
			tempList = new ArrayList<Object>();
			tempList.add(userInfo);
			attributeMap.put("userInfo", tempList);
		}
		
		if (refreshToken != null) {
			tempList = new ArrayList<Object>();
			tempList.add(refreshToken.getValue());
			attributeMap.put("refreshToken", tempList);
		}
		
		if (bat != null) {
			tempList = new ArrayList<Object>();
			tempList.add(bat);
			attributeMap.put("bearerAccessToken", tempList);
		}
		
		return attributeMap;
	}
	
	public IDTokenClaimsSet claimsFromMap(Map<String, List<Object>> attributeMap) {
		IDTokenClaimsSet claims;
		if (attributeMap.containsKey("claims") && 
				attributeMap.get("claims").get(0) instanceof IDTokenClaimsSet) {
			claims = (IDTokenClaimsSet) attributeMap.get("claims").get(0);
			return claims;
		}
		else {
			return null;
		}
	}
	
	public UserInfo userInfoFromMap(Map<String, List<Object>> attributeMap) {
		UserInfo userInfo;
		if (attributeMap.containsKey("userInfo") && 
				attributeMap.get("userInfo").get(0) instanceof UserInfo) {
			userInfo = (UserInfo) attributeMap.get("userInfo").get(0);
			return userInfo;
		}
		else {
			return null;
		}
	}
	
}
