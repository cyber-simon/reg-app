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
package edu.kit.scc.webreg.entity.as;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "ASUserAttrEntity")
@Table(name = "attribute_src_ua", 
	uniqueConstraints = @UniqueConstraint( columnNames = {"user_id", "attribute_src_id"}))
public class ASUserAttrEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
    @ManyToOne(targetEntity = AttributeSourceEntity.class)
    @JoinColumn(name = "attribute_src_id", nullable = false)
	private AttributeSourceEntity attributeSource;

    @Column(name = "last_query")
    private Date lastQuery;
    
    @Column(name = "last_successful_query")
    private Date lastSuccessfulQuery;
    
    @OneToMany(targetEntity = ASUserAttrValueEntity.class, mappedBy="asUserAttr")
    private Set<ASUserAttrValueEntity> values;
    
	@Enumerated(EnumType.STRING)
    @Column(name = "query_status")
	private AttributeSourceQueryStatus queryStatus;

    @Column(name = "queryMessage", length = 512)
    private String queryMessage;

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public AttributeSourceEntity getAttributeSource() {
		return attributeSource;
	}

	public void setAttributeSource(AttributeSourceEntity attributeSource) {
		this.attributeSource = attributeSource;
	}

	public Date getLastSuccessfulQuery() {
		return lastSuccessfulQuery;
	}

	public void setLastSuccessfulQuery(Date lastSuccessfulQuery) {
		this.lastSuccessfulQuery = lastSuccessfulQuery;
	}

	public Set<ASUserAttrValueEntity> getValues() {
		return values;
	}

	public void setValues(Set<ASUserAttrValueEntity> values) {
		this.values = values;
	}

	public Date getLastQuery() {
		return lastQuery;
	}

	public void setLastQuery(Date lastQuery) {
		this.lastQuery = lastQuery;
	}

	public AttributeSourceQueryStatus getQueryStatus() {
		return queryStatus;
	}

	public void setQueryStatus(AttributeSourceQueryStatus queryStatus) {
		this.queryStatus = queryStatus;
	}

	public String getQueryMessage() {
		return queryMessage;
	}

	public void setQueryMessage(String queryMessage) {
		this.queryMessage = queryMessage;
	}
}
