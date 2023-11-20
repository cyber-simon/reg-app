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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.kit.scc.regapp.sshkey.exc.UnsupportedKeyTypeException;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.ssh.OpenSshKeyDecoderOld;
import edu.kit.scc.webreg.ssh.OpenSshPublicKeyOld;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class UserSshKeyManagementOldBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	
	@Inject
	private Logger logger;
	
	@Inject
	private UserService userService;

	@Inject
	private IdentityService identityService;
	
    @Inject 
    private SessionManager sessionManager;

    @Inject
    private OpenSshKeyDecoderOld keyDecoder;

	@Inject
	private FacesMessageGenerator messageGenerator;

    private List<OpenSshPublicKeyOld> keyList;
    private String newKey;
    private String newName;
    private OpenSshPublicKeyOld selectedKey;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
			IdentityEntity identity = identityService.fetch(sessionManager.getIdentityId());
			List<UserEntity> userList = userService.findByIdentity(identity);
	    	user = userService.findByIdWithStore(userList.get(0).getId());

	    	keyList = new ArrayList<>();
	    	if (user.getGenericStore().containsKey("ssh_key")) {
		    	ObjectMapper om = new ObjectMapper();
		    	try {
					List<OpenSshPublicKeyOld> tempKeyList = om.readValue(user.getGenericStore().get("ssh_key"), 
							new TypeReference<List<OpenSshPublicKeyOld>>(){});
					for (OpenSshPublicKeyOld sshKey : tempKeyList) {
						try {
							keyList.add(keyDecoder.decode(sshKey));
						} catch (UnsupportedKeyTypeException e) {
							logger.warn("Unsupported key exception: ", e.getMessage());
						}
					}
				} catch (IOException e) {
					logger.warn("Could not read SSH keys from user: " + e.getMessage());
					messageGenerator.addResolvedErrorMessage("error_msg", "SSH Key not readable. Resetting keys.", false);
				}
	    	}
		}
	}

	public void deleteKey(String name) {
		int removeIndex = -1;
		
		for (int i=0; i<keyList.size(); i++) {
			if (keyList.get(i).getName().equals(name)) {
				removeIndex = i;
				break;
			}
		}
		
		if (removeIndex != -1) {
			keyList.remove(removeIndex);			
		}
		
		user.getGenericStore().put("ssh_key", buildSshKeyString());
		user = userService.save(user);
		messageGenerator.addResolvedInfoMessage("info", "ssh_key_deleted", false);				
	}
	
	public void deployKey() {
		OpenSshPublicKeyOld key;
		try {
			key = keyDecoder.decode(newName, newKey);
			keyList.add(key);
			user.getGenericStore().put("ssh_key", buildSshKeyString());
			user = userService.save(user);
			newKey = "";
			newName = "";
			if (key.getPublicKey() == null) {
				messageGenerator.addResolvedWarningMessage("warning", "ssh_key_unknown_format", false);
			} 
			else {
				messageGenerator.addResolvedInfoMessage("info", "ssh_key_deployed", false);				
			}
		} catch (UnsupportedKeyTypeException e) {
			logger.warn("An error occured whilst deploying key: " + e.getMessage());
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
		}
	}
	
	private String buildSshKeyString() {
		ObjectMapper om = new ObjectMapper();
		ArrayNode array = om.createArrayNode();
		for (OpenSshPublicKeyOld sshKey : keyList) {
			array.add(om.convertValue(sshKey, JsonNode.class));
		}
		return array.toString();
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

	public List<OpenSshPublicKeyOld> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<OpenSshPublicKeyOld> keyList) {
		this.keyList = keyList;
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public OpenSshPublicKeyOld getSelectedKey() {
		return selectedKey;
	}

	public void setSelectedKey(OpenSshPublicKeyOld selectedKey) {
		this.selectedKey = selectedKey;
	}
}
