package edu.kit.scc.webreg.service.identity;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.identity.IdentityUserPreferenceEntity;

@ApplicationScoped
public class IdentityUserPrefsResolver {

	public Map<String, Object> resolvePrefs(IdentityEntity identity) {
		
		Map<String, Object> prefsMap = new HashMap<String, Object>();

		if (identity.getUsers().size() == 0) {
			// nothing we can do here, case should not be reachable
		}
		else if (identity.getUsers().size() == 1) {
			// If there is only one account for identity, fill with values from account
			for(UserEntity user : identity.getUsers()) {
				prefsMap.put("email", user.getEmail());
				prefsMap.put("surName", user.getSurName());
				prefsMap.put("giveName", user.getGivenName());
				prefsMap.put("eppn", user.getEppn());
			}
		}
		else {
			// Fill in from userPref Account first
			UserEntity user = identity.getPrefUser();
			prefsMap.put("email", user.getEmail());
			prefsMap.put("surName", user.getSurName());
			prefsMap.put("giveName", user.getGivenName());
			prefsMap.put("eppn", user.getEppn());
			
			// User has more than one account. Take IdentityUserPreference into account
			// Overwrite the standard values from pref Account
			for (IdentityUserPreferenceEntity pref : identity.getUserPrefs()) {
				if (pref.getPrefType().equals("email"))
					prefsMap.put(pref.getPrefType(), pref.getUser().getEmail());
				else if (pref.getPrefType().equals("surName"))
					prefsMap.put(pref.getPrefType(), pref.getUser().getSurName());
				else if (pref.getPrefType().equals("giveName"))
					prefsMap.put(pref.getPrefType(), pref.getUser().getGivenName());
				else if (pref.getPrefType().equals("eppn"))
					prefsMap.put(pref.getPrefType(), pref.getUser().getEppn());
			}
		}
		
		return prefsMap;
	}
	
}
