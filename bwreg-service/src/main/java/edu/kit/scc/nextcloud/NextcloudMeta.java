package edu.kit.scc.nextcloud;

import jakarta.xml.bind.annotation.XmlElement;

public class NextcloudMeta {

	private String status;
	private int statusCode;
	private String message;
	private Integer totalItems;
	private Integer itemsPerPage;

	@XmlElement(name = "status", required = true)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlElement(name = "statuscode", required = true)
	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@XmlElement(name = "message", required = false)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@XmlElement(name = "totalitems", required = false)
	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(Integer totalItems) {
		this.totalItems = totalItems;
	}

	@XmlElement(name = "itemsperpage", required = false)
	public int getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(Integer itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
}
