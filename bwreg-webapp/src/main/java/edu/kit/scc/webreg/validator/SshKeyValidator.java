package edu.kit.scc.webreg.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;

import edu.kit.scc.regapp.sshkey.OpenSshKeyDecoder;
import edu.kit.scc.regapp.sshkey.OpenSshPublicKey;
import edu.kit.scc.regapp.sshkey.exc.UnsupportedKeyTypeException;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;

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
