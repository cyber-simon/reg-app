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
import java.util.List;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.regapp.sshkey.OpenSshKeyDecoder;
import edu.kit.scc.regapp.sshkey.OpenSshPublicKey;
import edu.kit.scc.regapp.sshkey.exc.UnsupportedKeyTypeException;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;

@Named
@ViewScoped
public class BulkSshKeyBlacklistImportBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SshPubKeyService sshPubKeyService;

    @Inject
    private OpenSshKeyDecoder keyDecoder;

	private String keyField;
	
	public void importKeys() {
		logger.info("Starting import");

		long importTime = System.currentTimeMillis();
		int importCount = 0;
		
		StringBuffer sb = new StringBuffer();
		
		String[] lines = keyField.split(System.getProperty("line.separator"));
		for (String line : lines) {
			line = line.trim();
			
			importCount++;
			
			OpenSshPublicKey key = new OpenSshPublicKey();
			SshPubKeyEntity entity = sshPubKeyService.createNew();
			entity.setEncodedKey(line);
			entity.setKeyStatus(SshPubKeyStatus.DELETED);
			entity.setName("imported-" + importTime + "-" + importCount);
			key.setPubKeyEntity(entity);

			logger.info("Importing line {}", importCount);
			
			try {
				keyDecoder.decode(key);
				entity.setEncodedKey(key.getBaseDate());
				
				List<SshPubKeyEntity> blackList = sshPubKeyService.findByKey(entity.getEncodedKey());
				if (blackList.size() == 0) {
					entity = sshPubKeyService.save(entity);
				}
				else {
					logger.info("Key {} already blacklisted", importCount);
				}
			} catch (UnsupportedKeyTypeException e) {
				logger.info("Key not supported: " + e.getMessage());
				sb.append(line);
				sb.append(System.getProperty("line.separator"));
			}
		}
		
		logger.info("Done import");
		
		keyField = sb.toString();
	}

	public String getKeyField() {
		return keyField;
	}

	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}
}
