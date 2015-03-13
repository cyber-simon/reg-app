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

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "ASUserAttrValueLongEntity")
public class ASUserAttrValueLongEntity extends ASUserAttrValueEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "value_long")
	private Long valueLong;

	public Long getValueLong() {
		return valueLong;
	}

	public void setValueLong(Long valueLong) {
		this.valueLong = valueLong;
	}
}
