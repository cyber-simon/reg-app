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

import java.io.IOException;

import edu.kit.scc.webreg.entity.IconCacheEntity;
import edu.kit.scc.webreg.entity.ImageType;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.service.disco.DiscoveryCacheService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/icon-cache")
public class IconCacheController {

	@Inject
	private DiscoveryCacheService discoveryCacheService;

	@Path(value = "/sync/small/{id}")
	@GET
	public Response getSyncFromCache(@PathParam("id") Long id) throws IOException, NoItemFoundException {

		IconCacheEntity icon = discoveryCacheService.getIconSync(id);
		
		if (icon == null) {
			throw new NoItemFoundException("no item found");
		}

		final String mimeType = resolveMimeType(icon.getImageType());
		if (mimeType == null) {
			throw new NoItemFoundException("icon has unsupported mime type");
		}
			
		return Response.ok(icon.getImageData().getData(), resolveMimeType(icon.getImageType())).build();
	}

	@Path(value = "/async/small/{id}")
	@GET
	public Response getAsyncFromCache(@PathParam("id") Long id) throws IOException, NoItemFoundException {

		IconCacheEntity icon = discoveryCacheService.getIconAsync(id);
		
		if (icon == null) {
			throw new NoItemFoundException("no item found");
		}

		final String mimeType = resolveMimeType(icon.getImageType());
		if (mimeType == null) {
			throw new NoItemFoundException("icon has unsupported mime type");
		}
			
		return Response.ok(icon.getImageData().getData(), resolveMimeType(icon.getImageType())).build();
	}

	@Path(value = "/sync/large/{id}")
	@GET
	public Response getSyncFromCacheLarge(@PathParam("id") Long id) throws IOException, NoItemFoundException {

		IconCacheEntity icon = discoveryCacheService.getIconSync(id);
		
		if (icon == null) {
			throw new NoItemFoundException("no item found");
		}

		return Response.ok(icon.getImageData().getData(), resolveMimeType(icon.getImageType())).build();
	}

	private String resolveMimeType(ImageType type) {
		if (type == null)
			return null;
		else if (type.equals(ImageType.PNG)) 
			return "image/png";
		else if (type.equals(ImageType.JPEG))
			return "image/jpeg";
		else if (type.equals(ImageType.SVG))
			return "image/svg+xml";
		else
			return null;
	}
}
