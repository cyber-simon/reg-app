package edu.kit.scc.webreg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "UserProvisionerEntity")
@Table(name = "user_provisioner")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserProvisionerEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = IconCacheEntity.class)
	private IconCacheEntity icon;

	@ManyToOne(targetEntity = IconCacheEntity.class)
	private IconCacheEntity iconLarge;

	@Column(name = "org_name", length = 4096)
	private String orgName;
	
	@Column(name = "description", length = 4096)
	private String description;
	
	@Column(name = "information_url", length = 4096)
	private String informationUrl;

	@Column(name = "display_name", length = 4096)
	private String displayName;
		
	public IconCacheEntity getIcon() {
		return icon;
	}

	public void setIcon(IconCacheEntity icon) {
		this.icon = icon;
	}

	public IconCacheEntity getIconLarge() {
		return iconLarge;
	}

	public void setIconLarge(IconCacheEntity iconLarge) {
		this.iconLarge = iconLarge;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInformationUrl() {
		return informationUrl;
	}

	public void setInformationUrl(String informationUrl) {
		this.informationUrl = informationUrl;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
