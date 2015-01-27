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
package edu.kit.scc.webreg.converter;

import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;
import edu.kit.scc.webreg.service.ImageService;

@Named("imageConverter")
public class ImageConverter extends AbstractConverter {

	private static final long serialVersionUID = 1L;

	@Inject
	private ImageService service;

	@Override
	protected BaseService<? extends BaseEntity<Long>, Long> getService() {
		return service;
	}
	
}
