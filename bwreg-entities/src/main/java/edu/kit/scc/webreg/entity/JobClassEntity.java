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
package edu.kit.scc.webreg.entity;

import java.util.Map;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="job_class")
public class JobClassEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@Column(name = "job_class_name", nullable = false, length = 256)
	private String jobClassName;

	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "job_store")
    @MapKeyColumn(name = "key_data", length = 1024)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> jobStore; 
	
	@OneToMany(targetEntity = JobScheduleEntity.class, mappedBy="jobClass")
	private Set<JobScheduleEntity> schedules;
	
	private Boolean singleton;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getJobStore() {
		return jobStore;
	}

	public void setJobStore(Map<String, String> jobStore) {
		this.jobStore = jobStore;
	}

	public String getJobClassName() {
		return jobClassName;
	}

	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}

	public Boolean getSingleton() {
		return singleton;
	}

	public void setSingleton(Boolean singleton) {
		this.singleton = singleton;
	}

	public Set<JobScheduleEntity> getSchedules() {
		return schedules;
	}

	public void setSchedules(Set<JobScheduleEntity> schedules) {
		this.schedules = schedules;
	}
}
