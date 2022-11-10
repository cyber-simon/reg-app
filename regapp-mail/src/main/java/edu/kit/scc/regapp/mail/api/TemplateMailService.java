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
package edu.kit.scc.regapp.mail.api;

import java.io.Serializable;
import java.util.Map;

public interface TemplateMailService extends Serializable {

	void sendMail(String templateName, Map<String, Object> rendererContext,
			Boolean queued);

}
