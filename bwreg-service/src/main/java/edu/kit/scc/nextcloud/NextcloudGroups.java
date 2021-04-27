package edu.kit.scc.nextcloud;

import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlElement;

public class NextcloudGroups {

    private ArrayList<String> groupList;

    @XmlElement(name = "element")
	public ArrayList<String> getGroupList() {
		return groupList;
	}

	public void setGroupList(ArrayList<String> groupList) {
		this.groupList = groupList;
	}    
}
