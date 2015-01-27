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
package edu.kit.scc.webreg.bean.admin.bulk;

import java.io.Serializable;

public class RegisterUser implements Serializable {

	private static final long serialVersionUID = 1L;

	private String eppn;
	private String status;
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEppn() {
		return eppn;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eppn == null) ? 0 : eppn.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegisterUser other = (RegisterUser) obj;
		if (eppn == null) {
			if (other.eppn != null)
				return false;
		} else if (!eppn.equals(other.eppn))
			return false;
		return true;
	}
}
