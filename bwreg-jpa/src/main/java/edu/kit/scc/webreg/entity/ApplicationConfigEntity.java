package edu.kit.scc.webreg.entity;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity(name = "ApplicationConfigEntity")
@Table(name = "application_config")
public class ApplicationConfigEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "format_version", length = 128)
	private String configFormatVersion;
	
	@Column(name = "sub_version", length = 128)
	private String subVersion;
	
	@Column(name = "activeConfig")
	private Boolean activeConfig;

	@Column(name = "dirty_stamp")
	private Date dirtyStamp;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "application_config_options")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> configOptions;

	public String getSubVersion() {
		return subVersion;
	}

	public void setSubVersion(String subVersion) {
		this.subVersion = subVersion;
	}

	public Boolean getActiveConfig() {
		return activeConfig;
	}

	public void setActiveConfig(Boolean activeConfig) {
		this.activeConfig = activeConfig;
	}

	public Map<String, String> getConfigOptions() {
		return configOptions;
	}

	public void setConfigOptions(Map<String, String> configOptions) {
		this.configOptions = configOptions;
	}

	public String getConfigFormatVersion() {
		return configFormatVersion;
	}

	public void setConfigFormatVersion(String configFormatVersion) {
		this.configFormatVersion = configFormatVersion;
	}

	public Date getDirtyStamp() {
		return dirtyStamp;
	}

	public void setDirtyStamp(Date dirtyStamp) {
		this.dirtyStamp = dirtyStamp;
	} 

}
