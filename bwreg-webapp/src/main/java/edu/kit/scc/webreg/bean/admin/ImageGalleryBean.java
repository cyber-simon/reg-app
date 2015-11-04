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
package edu.kit.scc.webreg.bean.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.ImageDataEntity;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.ImageType;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.ImageService;
import edu.kit.scc.webreg.service.ServiceService;

@ManagedBean
@ViewScoped
public class ImageGalleryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(ImageGalleryBean.class);
	
	@Inject
	private ImageService imageService;
	
	@Inject
	private ServiceService serviceService;
	
	private List<ImageEntity> imageList;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (imageList == null) {
			imageList = imageService.findAll();
		}
	}

	public List<ImageEntity> getImageList() {
		return imageList;
	}	

	public void deleteImage(ImageEntity image) {

		try {
			List<ServiceEntity> serviceList = serviceService.findAllByImage(image);
			
			if (serviceList.size() > 0) {
				StringBuffer serviceBuffer = new StringBuffer();
				serviceBuffer.append("Services: ");
				for (ServiceEntity service : serviceList) { 
					serviceBuffer.append(service.getName());
					serviceBuffer.append(" ");
				}
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Bild wird nocht verwendet", serviceBuffer.toString());
				FacesContext.getCurrentInstance().addMessage("messageBox", msg);
				imageList = imageService.findAll();
				return;
			}
			
			imageService.delete(image);
		}
		catch (Throwable e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bild kann nicht gelöscht werden", e.getMessage());
			FacesContext.getCurrentInstance().addMessage("messageBox", msg);
			imageList = imageService.findAll();
			return;
		}

		imageList = imageService.findAll();

		FacesMessage msg = new FacesMessage("Gelöscht", image.getName());
		FacesContext.getCurrentInstance().addMessage("messageBox", msg);		
	}
	
    public void handleFileUpload(FileUploadEvent event) {
		logger.debug("A file was uploaded: {}", event.getFile().getFileName());
		
		try {
			ImageEntity image = imageService.createNew();
			image.setImageData(new ImageDataEntity());
			
			image.setName(event.getFile().getFileName());
			image.setImageType(ImageType.PNG);
			image.getImageData().setData(IOUtils.toByteArray(event.getFile().getInputstream()));
			imageService.save(image);
			
			imageList = imageService.findAll();

			FacesMessage msg = new FacesMessage("Hochgeladen", event.getFile().getFileName());
			FacesContext.getCurrentInstance().addMessage("messageBox", msg);
		} catch (IOException e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Bild Daten fehlen", e.getMessage());
			FacesContext.getCurrentInstance().addMessage("messageBox", msg);
			logger.warn("Exception while upload of image", e);
		}
	}

	
}
