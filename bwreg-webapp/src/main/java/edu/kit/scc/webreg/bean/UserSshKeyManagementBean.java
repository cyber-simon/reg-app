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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.regapp.sshkey.OpenSshKeyDecoder;
import edu.kit.scc.regapp.sshkey.OpenSshPublicKey;
import edu.kit.scc.regapp.sshkey.exc.SshPubKeyBlacklistedException;
import edu.kit.scc.regapp.sshkey.exc.UnsupportedKeyTypeException;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class UserSshKeyManagementBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private IdentityEntity identity;

	@Inject
	private Logger logger;

	@Inject
	private IdentityService identityService;

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
		if (identity == null) {
			identity = identityService.fetch(sessionManager.getIdentityId());
			List<SshPubKeyEntity> sshPubKeyList = new ArrayList<>();
			sshPubKeyList.addAll(sshPubKeyService.findByIdentityAndStatusWithRegs(identity.getId(), SshPubKeyStatus.ACTIVE));
			sshPubKeyList.addAll(sshPubKeyService.findByIdentityAndStatusWithRegs(identity.getId(), SshPubKeyStatus.EXPIRED));

			keyList = new ArrayList<>();
			for (SshPubKeyEntity sshKey : sshPubKeyList) {
				OpenSshPublicKey key = new OpenSshPublicKey();
				key.setPubKeyEntity(sshKey);
				keyList.add(key);
				try {
					keyDecoder.decode(key);
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

		for (int i = 0; i < keyList.size(); i++) {
			if (keyList.get(i).getPubKeyEntity().getName().equals(name)) {
				removeIndex = i;
				removeEntity = keyList.get(i).getPubKeyEntity();
				break;
			}
		}

		if (removeIndex != -1) {
			keyList.remove(removeIndex);
			sshPubKeyService.deleteKey(removeEntity, "identity-" + identity.getId());
		}

		messageGenerator.addResolvedInfoMessage("info", "ssh_keys.key_deleted", false);
	}

	public void deployKey() {
		OpenSshPublicKey key = new OpenSshPublicKey();
		SshPubKeyEntity sshPubKeyEntity = sshPubKeyService.createNew();
		key.setPubKeyEntity(sshPubKeyEntity);

		try {
			sshPubKeyEntity.setName(newName);
			sshPubKeyEntity.setEncodedKey(newKey);
			sshPubKeyEntity.setIdentity(identity);
			sshPubKeyEntity.setKeyStatus(SshPubKeyStatus.ACTIVE);

			keyDecoder.decode(key);

			sshPubKeyEntity.setEncodedKey(key.getBaseDate());

			Long expireTime = 90 * 24 * 60 * 60 * 1000L; // 90 days standard expiry time for ssh keys. -1 for never expire
			if (appConfig.getConfigValue("sshpubkey_expire_time") != null) {
				expireTime = Long.parseLong(appConfig.getConfigValue("sshpubkey_expire_time"));
			}

			if (expireTime != -1) {
				sshPubKeyEntity.setExpiresAt(new Date(System.currentTimeMillis() + expireTime));
			}

			sshPubKeyEntity = sshPubKeyService.deployKey(identity.getId(), sshPubKeyEntity, "identity-" + identity.getId());
			keyList.add(key);
			newKey = "";
			newName = "";
			messageGenerator.addResolvedInfoMessage("info", "ssh_key_deployed", true);
		} catch (UnsupportedKeyTypeException e) {
			logger.warn("An error occured whilst deploying key: " + e.getMessage());
			messageGenerator.addResolvedErrorMessage("sshKeyMessage", "error_msg", e.toString(), false);
		} catch (SshPubKeyBlacklistedException e) {
			logger.warn("User {} tried to deploy blacklisted key", identity.getId());
			messageGenerator.addResolvedErrorMessage("sshKeyMessage", "error", "key_blacklisted", true);
		}
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

	public IdentityEntity getIdentity() {
		return identity;
	}

}
