package edu.kit.scc.webreg.service.disco;

import java.io.Serializable;

public class UserProvisionerCachedEntry implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String name;
	private String orgName;
	private Long iconId;
	private Long iconLargeId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserProvisionerCachedEntry other = (UserProvisionerCachedEntry) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Long getIconId() {
		return iconId;
	}

	public void setIconId(Long iconId) {
		this.iconId = iconId;
	}

	public Long getIconLargeId() {
		return iconLargeId;
	}

	public void setIconLargeId(Long iconLargeId) {
		this.iconLargeId = iconLargeId;
	}
}
