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
package edu.kit.scc.webreg.rest;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.service.ImageService;

@Path("/image")
public class ImageController {

	@Inject
	private ImageService imageService;
	
	@Path(value = "/original/{imageId}")
	@Produces({"image/png"})
	@GET
	public Response deliverOriginal(@PathParam("imageId") Long imageId)
					throws IOException, NoItemFoundException {
		
		ImageEntity imageEntity = imageService.findByIdWithData(imageId);
		if (imageEntity == null) {
			throw new NoItemFoundException("no item found");
		}

		return Response.ok(imageEntity.getImageData().getData(), "image/png").build();
	}

	@Path(value = "/small/{imageId}")
	@Produces({"image/png"})
	@GET
	public Response deliverSmall(@PathParam("imageId") Long imageId)
					throws IOException, NoItemFoundException {
		ImageEntity imageEntity = imageService.findByIdWithData(imageId);
		if (imageEntity == null) {
			throw new NoItemFoundException("no item found");
		}

		byte[] data = scale(imageEntity.getImageData().getData(), 100, 100, "image/png");

		return Response.ok(data, "image/png").build();
	}
	
	@Path(value = "/icon/{imageId}")
	@Produces({"image/png"})
	@GET
	public Response deliverIcon(@PathParam("imageId") Long imageId)
					throws IOException, NoItemFoundException {
		ImageEntity imageEntity = imageService.findByIdWithData(imageId);
		if (imageEntity == null) {
			throw new NoItemFoundException("no item found");
		}
		
		byte[] data = scale(imageEntity.getImageData().getData(), 32, 32, "image/png");

		return Response.ok(data, "image/png").build();
	}
	
	private byte[] scale(byte[] input, int width, int height, String format) throws IOException {
		ImageIO.setUseCache(false);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(input));
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;

		if (height == 0) {
			height = (width * img.getHeight()) / img.getWidth();
		}
		if(width == 0) {
			width = (height * img.getWidth()) / img.getHeight();
		}

		BufferedImage scaledImage = new BufferedImage(width, height, type);
		Graphics2D g2 = scaledImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.drawImage(img, 0, 0, width, height, null);
		g2.dispose();

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		ImageIO.write(scaledImage, "png", buffer);

		return buffer.toByteArray();
	}

}
