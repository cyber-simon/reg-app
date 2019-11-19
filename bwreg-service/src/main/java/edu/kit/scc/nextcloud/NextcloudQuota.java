package edu.kit.scc.nextcloud;

public class NextcloudQuota {

	private Long free;
	private Long used;
	private Long total;
	private Double relative;
	private Long quota;
	
	public Long getFree() {
		return free;
	}
	
	public void setFree(Long free) {
		this.free = free;
	}

	public Long getUsed() {
		return used;
	}

	public void setUsed(Long used) {
		this.used = used;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Double getRelative() {
		return relative;
	}

	public void setRelative(Double relative) {
		this.relative = relative;
	}

	public Long getQuota() {
		return quota;
	}

	public void setQuota(Long quota) {
		this.quota = quota;
	}
	
}
