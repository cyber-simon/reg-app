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
package edu.kit.scc.webreg.bean.admin.bulk;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

public class ImportUser implements Serializable {

	private static final long serialVersionUID = 1L;

	private String uid;
	private String persistentId;
	
	private String spEntityId;
	private String idpEntityId;
	
	private String status;
	
	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getPersistentId() {
		return persistentId;
	}
	
	public void generatePersistentId(String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		if (uid == null)
			throw new IllegalStateException("uid must not be null");
		
		String text = spEntityId + "!" + uid + "!" + salt;
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] bytes = text.getBytes(("UTF-8"));
		md.update(bytes);
		byte[] digest = md.digest();
		persistentId = new String(Base64.encodeBase64(digest));
	}

	public String getSpEntityId() {
		return spEntityId;
	}

	public void setSpEntityId(String spEntityId) {
		this.spEntityId = spEntityId;
	}

	public String getIdpEntityId() {
		return idpEntityId;
	}

	public void setIdpEntityId(String idpEntityId) {
		this.idpEntityId = idpEntityId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
