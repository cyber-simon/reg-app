package edu.kit.scc.webreg.service.disco;

import java.util.Comparator;

public class UserProvisionerComparator implements Comparator<UserProvisionerCachedEntry> {

	@Override
	public int compare(UserProvisionerCachedEntry e1, UserProvisionerCachedEntry e2) {
		if (e1.getDisplayName() != null)
			return e1.getDisplayName().compareTo(e2.getDisplayName());
		else 
			return 0;
	}
}
