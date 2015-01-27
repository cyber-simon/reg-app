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
package edu.kit.scc.webreg.service.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ImageDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.service.ImageService;

@Stateless
public class ImageServiceImpl extends BaseServiceImpl<ImageEntity, Long> implements ImageService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ImageDao dao;
	
	@Override
	public ImageEntity findByName(String name) {
		return dao.findByName(name);
	}

	@Override
	public ImageEntity findByIdWithData(Long id) {
		return dao.findByIdWithData(id);
	}

	@Override
	protected BaseDao<ImageEntity, Long> getDao() {
		return dao;
	}
	
}
