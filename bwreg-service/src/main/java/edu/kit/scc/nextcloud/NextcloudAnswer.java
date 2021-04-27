package edu.kit.scc.nextcloud;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ocs")
public class NextcloudAnswer {

	private NextcloudMeta meta;
	private NextcloudUser user;

	@XmlElement(name = "meta", required = true)
	public NextcloudMeta getMeta() {
		return meta;
	}

	public void setMeta(NextcloudMeta meta) {
		this.meta = meta;
	}

	@XmlElement(name = "data", required = true)
	public NextcloudUser getUser() {
		return user;
	}

	public void setUser(NextcloudUser user) {
		this.user = user;
	}
}
