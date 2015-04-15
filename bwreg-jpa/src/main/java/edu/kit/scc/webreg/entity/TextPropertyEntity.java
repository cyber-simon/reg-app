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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "text_property", 
	uniqueConstraints = @UniqueConstraint(columnNames = {"text_key", "text_lang"}))
public class TextPropertyEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "text_key", nullable = false, length = 256)
	private String key;

	@Column(name = "text_value", nullable = false, length = 2048)
	private String value;

	@Column(name = "text_lang", nullable = false, length = 32)
	private String language;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
