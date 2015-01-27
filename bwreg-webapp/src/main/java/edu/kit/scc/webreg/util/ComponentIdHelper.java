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
package edu.kit.scc.webreg.util;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("componentIdHelper")
@ApplicationScoped
public class ComponentIdHelper {

	public String generateFor(String label) {
		if (label == null || label.isEmpty())
			label = UUID.randomUUID().toString();

		return label.toLowerCase().replaceAll("[^a-zA-Z]", "");
	}
	
}
