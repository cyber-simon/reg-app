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
package edu.kit.scc.webreg.entity.audit;

import jakarta.persistence.Entity;

@Entity(name = "AuditUserDeleteEntity")
public class AuditUserDeleteEntity extends AuditUserEntity {

	private static final long serialVersionUID = 1L;

}
