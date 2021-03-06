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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "ImageEntity")
@Table(name = "image")
public class ImageEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 128)
	private String name;

	@Enumerated(EnumType.STRING)
	private ImageType imageType;
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
			targetEntity = ImageDataEntity.class)
	private ImageDataEntity imageData;
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ImageDataEntity getImageData() {
		return imageData;
	}

	public void setImageData(ImageDataEntity imageData) {
		this.imageData = imageData;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
	}

}
