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
package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.TextPropertyEntity;

public interface TextPropertyService extends BaseService<TextPropertyEntity, Long> {

	TextPropertyEntity findAllBySingleton(String key, String language);

}
