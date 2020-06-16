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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.ssh.OpenSshKeyDecoder;
import edu.kit.scc.webreg.ssh.OpenSshPublicKey;
import edu.kit.scc.webreg.ssh.SshPubKeyBlacklistedException;
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

	@Inject
	private SshPubKeyService sshPubKeyService;
	
	@Inject
	private ApplicationConfig appConfig;
	
    private List<OpenSshPublicKey> keyList;
    private String newKey;
    private String newName;
    private OpenSshPublicKey selectedKey;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
	    	user = userService.findById(sessionManager.getUserId());
	    	List<SshPubKeyEntity> sshPubKeyList = sshPubKeyService.findByUserAndStatus(user.getId(), SshPubKeyStatus.ACTIVE);
	    	
	    	keyList = new ArrayList<>();
	    	for (SshPubKeyEntity sshKey : sshPubKeyList) {
				try {
					keyList.add(keyDecoder.decode(sshKey));
				} catch (UnsupportedKeyTypeException e) {
					logger.warn("Unsupported key exception: ", e.getMessage());
					messageGenerator.addResolvedErrorMessage("error_msg", "SSH Key not readable.", false);
				}
	    	}
		}
	}

	public void deleteKey(String name) {
		int removeIndex = -1;
		SshPubKeyEntity removeEntity = null;
		
		for (int i=0; i<keyList.size(); i++) {
			if (keyList.get(i).getPubKeyEntity().getName().equals(name)) {
				removeIndex = i;
				removeEntity = keyList.get(i).getPubKeyEntity();
				break;
			}
		}
		
		if (removeIndex != -1) {
			keyList.remove(removeIndex);
			removeEntity.setKeyStatus(SshPubKeyStatus.DELETED);
			removeEntity = sshPubKeyService.save(removeEntity);
		}
		
		messageGenerator.addResolvedInfoMessage("info", "ssh_key_deleted", false);				
	}
	
	public void deployKey() {
		OpenSshPublicKey key;

		Long expireTime = 90 * 24 * 60 * 60 * 1000L; // 90 days standard expiry time for ssh keys. -1 for never expire
		if (appConfig.getConfigValue("sshpubkey_expire_time") != null) {
			expireTime = Long.parseLong(appConfig.getConfigValue("sshpubkey_expire_time"));
		}

		SshPubKeyEntity sshPubKeyEntity = sshPubKeyService.createNew();
		sshPubKeyEntity.setName(newName);
		sshPubKeyEntity.setEncodedKey(newKey);
		sshPubKeyEntity.setUser(user);
		sshPubKeyEntity.setKeyStatus(SshPubKeyStatus.ACTIVE);

		if (expireTime != -1) {
			sshPubKeyEntity.setExpiresAt(new Date(System.currentTimeMillis() + expireTime));			
		}
		
		try {
			key = keyDecoder.decode(sshPubKeyEntity);
			sshPubKeyEntity = sshPubKeyService.deployKey(user.getId(), sshPubKeyEntity);
			keyList.add(key);
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
		} catch (SshPubKeyBlacklistedException e) {
			logger.warn("User {} tried to deploy blacklisted key", user.getId());
			messageGenerator.addResolvedErrorMessage("error", "key_blacklisted", false);
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

	public OpenSshPublicKey getSelectedKey() {
		return selectedKey;
	}

	public void setSelectedKey(OpenSshPublicKey selectedKey) {
		this.selectedKey = selectedKey;
	}
}
