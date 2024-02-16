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

import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "IconCacheEntity")
@Table(name = "icon_cache")
public class IconCacheEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", length = 128)
	private String name;

	@Column(name = "url", length = 4096)
	private String url;

	@Column(name = "valid_until")
	private Date validUntil;

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

}
