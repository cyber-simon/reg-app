package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;

public class LinotpResultSet implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer tokens;
	private Integer pages;
	private Integer pagesize;
	private Integer page;

	public Integer getTokens() {
		return tokens;
	}
	
	public void setTokens(Integer tokens) {
		this.tokens = tokens;
	}
	
	public Integer getPages() {
		return pages;
	}
	
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	
	public Integer getPagesize() {
		return pagesize;
	}
	
	public void setPagesize(Integer pagesize) {
		this.pagesize = pagesize;
	}
	
	public Integer getPage() {
		return page;
	}
	
	public void setPage(Integer page) {
		this.page = page;
	}
}
