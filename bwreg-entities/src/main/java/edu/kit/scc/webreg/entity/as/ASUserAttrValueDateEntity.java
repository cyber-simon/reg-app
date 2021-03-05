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

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "ASUserAttrValueDateEntity")
public class ASUserAttrValueDateEntity extends ASUserAttrValueEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "value_date")
	private Date valueDate;

	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}
}
