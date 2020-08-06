package edu.kit.scc.webreg.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;
import edu.kit.scc.webreg.ssh.OpenSshKeyDecoder;
import edu.kit.scc.webreg.ssh.OpenSshPublicKey;
import edu.kit.scc.webreg.ssh.UnsupportedKeyTypeException;

@FacesValidator("edu.kit.SshKeyValidator")
public class SshKeyValidator implements Validator {

	@Inject
	private SshPubKeyService sshPubKeyService;

    @Inject
    private OpenSshKeyDecoder keyDecoder;

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		OpenSshPublicKey key = new OpenSshPublicKey();
		SshPubKeyEntity sshPubKeyEntity = sshPubKeyService.createNew();
		key.setPubKeyEntity(sshPubKeyEntity);

		try {
			if (value != null) {
				sshPubKeyEntity.setEncodedKey(value.toString());
				keyDecoder.decode(key);
			}
		} catch (UnsupportedKeyTypeException e) {
			FacesMessage msg =
					new FacesMessage("SSH Key validation failed.",
							"Invalid format: " + e.getMessage());
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(msg);
		} 
	}

}
