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
package edu.kit.scc.webreg.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.ssh.OpenSshKeyDecoder;
import edu.kit.scc.webreg.ssh.OpenSshPublicKey;
import edu.kit.scc.webreg.ssh.UnsupportedKeyTypeException;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class UserSshKeyManagementBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	
	@Inject
	private Logger logger;
	
	@Inject
	private UserService userService;
	
    @Inject 
    private SessionManager sessionManager;

    @Inject
    private OpenSshKeyDecoder keyDecoder;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
    private List<OpenSshPublicKey> keyList;
    private String newKey;
    private String newName;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
	    	user = userService.findByIdWithStore(sessionManager.getUserId());
	    	if (user.getGenericStore().containsKey("ssh_key")) {
		    	ObjectMapper om = new ObjectMapper();
		    	try {
					keyList = om.readValue(user.getGenericStore().get("ssh_key"), 
							new TypeReference<List<OpenSshPublicKey>>(){});
				} catch (IOException e) {
					logger.warn("Could not read SSH keys from user: " + e.getMessage());
					messageGenerator.addResolvedErrorMessage("error_msg", "SSH Key not readable. Resetting keys.", false);
		    		keyList = new ArrayList<>();
				}
	    	}
	    	else {
	    		keyList = new ArrayList<>();
	    	}
		}
	}
	
	public void deployKey() {
		OpenSshPublicKey key;
		try {
			key = keyDecoder.decode(newName, newKey);
			keyList.add(key);
			ObjectMapper om = new ObjectMapper();
			ArrayNode array = om.createArrayNode();
			for (OpenSshPublicKey sshKey : keyList) {
				array.add(om.convertValue(sshKey, JsonNode.class));
			}
			user.getGenericStore().put("ssh_key", array.toString());
			user = userService.save(user);
			newKey = "";
			newName = "";
		} catch (UnsupportedKeyTypeException e) {
			logger.warn("An error occured whilst deploying key: " + e.getMessage());
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
		}
	}
	
	public UserEntity getUser() {
		return user;
	}

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public List<OpenSshPublicKey> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<OpenSshPublicKey> keyList) {
		this.keyList = keyList;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}
}
