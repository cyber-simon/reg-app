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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "VelocityTemplateEntity")
@Table(name = "velocity_template")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class VelocityTemplateEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "tpl_name", length=256)
	private String name;
	
	@Column(name = "tpl_content")
	@Lob 
	@Type(type = "org.hibernate.type.TextType")	
	private String template;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
}
