package edu.kit.scc.webreg.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity(name = "ClusterMemberEntity")
@Table(name = "cluster_member")
public class ClusterMemberEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "node_name", length = 128)
	private String nodeName;

	@Enumerated(EnumType.STRING)
	private ClusterMemberStatus clusterMemberStatus;
	
	@Column(name = "last_status_change")
	private Date lastStatusChange;
	
	@Column(name = "last_status_check")
	private Date lastStatusCheck;
	
	@Enumerated(EnumType.STRING)
	private ClusterSchedulerStatus clusterSchedulerStatus;
	
	@Column(name = "last_scheduler_status_change")
	private Date lastSchedulerStatusChange;
	
	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ClusterMemberStatus getClusterMemberStatus() {
		return clusterMemberStatus;
	}

	public void setClusterMemberStatus(ClusterMemberStatus clusterMemberStatus) {
		this.clusterMemberStatus = clusterMemberStatus;
	}

	public ClusterSchedulerStatus getClusterSchedulerStatus() {
		return clusterSchedulerStatus;
	}

	public void setClusterSchedulerStatus(ClusterSchedulerStatus clusterSchedulerStatus) {
		this.clusterSchedulerStatus = clusterSchedulerStatus;
	}

	public Date getLastSchedulerStatusChange() {
		return lastSchedulerStatusChange;
	}

	public void setLastSchedulerStatusChange(Date lastSchedulerStatusChange) {
		this.lastSchedulerStatusChange = lastSchedulerStatusChange;
	}

	public Date getLastStatusChange() {
		return lastStatusChange;
	}

	public void setLastStatusChange(Date lastStatusChange) {
		this.lastStatusChange = lastStatusChange;
	}

	public Date getLastStatusCheck() {
		return lastStatusCheck;
	}

	public void setLastStatusCheck(Date lastStatusCheck) {
		this.lastStatusCheck = lastStatusCheck;
	}
	
}
