/*******************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html Contributors: Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import java.io.IOException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

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
			sshPubKeyList.addAll(sshPubKeyService.findByIdentityAndStatusWithRegsAndUser(identity.getId(), SshPubKeyStatus.ACTIVE));
			sshPubKeyList.addAll(sshPubKeyService.findByIdentityAndStatusWithRegsAndUser(identity.getId(), SshPubKeyStatus.EXPIRED));

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
		keyList.stream().filter(k -> name.equals(k.getPubKeyEntity().getName())).findAny().ifPresent(k -> {
			keyList.remove(k);
			sshPubKeyService.deleteKey(k.getPubKeyEntity(), "identity-" + identity.getId());
		});
		messageGenerator.addResolvedInfoMessage("info", "ssh_keys.key_deleted", true);
	}

	public void deployKey() {
		OpenSshPublicKey key = new OpenSshPublicKey();
		SshPubKeyEntity sshPubKeyEntity = sshPubKeyService.createNew();
		key.setPubKeyEntity(sshPubKeyEntity);

		try {
			sshPubKeyEntity.setName(newName);
			sshPubKeyEntity.setEncodedKey(newKey);
			sshPubKeyEntity.setIdentity(identity);
			sshPubKeyEntity.setUser(identity.getPrefUser());
			sshPubKeyEntity.setKeyStatus(SshPubKeyStatus.ACTIVE);

			keyDecoder.decode(key);

			sshPubKeyEntity.setEncodedKey(key.getBaseDate());

			Long expireTime = 90 * 24 * 60 * 60 * 1000L; // 90 days standard expiry time for ssh keys. -1 for never expire
			if (appConfig.getConfigValue("sshpubkey_expire_time") != null) {
				expireTime = Long.parseLong(appConfig.getConfigValue("sshpubkey_expire_time"));
			}

			if (sshPubKeyEntity.getKeyType() != null && sshPubKeyEntity.getKeyType().equals("sk-ssh-ed25519@openssh.com") && 
					appConfig.getConfigValue("sshpubkey_fido_expire_time") != null) {
				expireTime = Long.parseLong(appConfig.getConfigValue("sshpubkey_fido_expire_time"));
			}
			
			if (expireTime != -1) {
				sshPubKeyEntity.setExpiresAt(new Date(System.currentTimeMillis() + expireTime));
			}

			sshPubKeyEntity = sshPubKeyService.deployKey(identity.getId(), sshPubKeyEntity, "identity-" + identity.getId());
			keyList.add(key);
			newKey = "";
			newName = "";
			messageGenerator.addResolvedInfoMessage("info", "ssh_key_deployed", true);
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
		} catch (UnsupportedKeyTypeException e) {
			logger.warn("An error occured whilst deploying key: " + e.getMessage());
			messageGenerator.addResolvedErrorMessage("sshKeyMessage", "error_msg", e.toString(), false);
		} catch (SshPubKeyBlacklistedException e) {
			logger.warn("User {} tried to deploy blacklisted key", identity.getId());
			messageGenerator.addResolvedErrorMessage("sshKeyMessage", "error", "key_blacklisted", true);
		} catch (IOException e) {
			logger.warn("An error occured trying to reload page after deploying key: " + e.getMessage());
			messageGenerator.addResolvedErrorMessage("sshKeyMessage", "error_msg", e.toString(), false);
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
