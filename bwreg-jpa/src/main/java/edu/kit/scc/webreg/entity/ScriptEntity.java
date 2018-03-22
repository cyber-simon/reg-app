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
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "ScriptEntity")
@Table(name = "script")
public class ScriptEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "script_text")
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String script;
	
	@Column(name = "rule_type", length = 32)
	private String scriptType;
	
	@Column(name = "name", length = 128)
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
}
