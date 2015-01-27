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
package edu.kit.scc.webreg.model;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public interface GenericLazyDataModel<E extends BaseEntity<PK>, T extends BaseService<E, PK>, PK extends Serializable>  extends Serializable {

}
