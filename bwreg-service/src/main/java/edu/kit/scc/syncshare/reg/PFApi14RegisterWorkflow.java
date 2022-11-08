package edu.kit.scc.syncshare.reg;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.lsdf.sns.service.PFAccount;
import edu.kit.lsdf.sns.service.PFApi14Worker;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.Infotainment;
import edu.kit.scc.webreg.service.reg.InfotainmentCapable;
import edu.kit.scc.webreg.service.reg.InfotainmentTreeNode;
import edu.kit.scc.webreg.service.reg.RegisterUserWorkflow;
import edu.kit.scc.webreg.tools.PropertyReader;

public class PFApi14RegisterWorkflow implements RegisterUserWorkflow, InfotainmentCapable {

	private static final Logger logger = LoggerFactory.getLogger(PFApi14RegisterWorkflow.class);
	
	@Override
	public void registerUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		PFApi14Worker pfWorker = new PFApi14Worker(prop, auditor);

		String spaceAllowed;
		if (prop.hasProp("space_allowed")) 
			spaceAllowed = prop.readPropOrNull("space_allowed");
		else 
			spaceAllowed = "" + 1024L * 1024L * 1024L * 10L;

		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		
		PFAccount pfAccount = new PFAccount();
		
		pfAccount.setUsername(user.getEppn());
		if (user.getEmail() != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(user.getEmail().replace(";", ","));
			
			if (user.getEmailAddresses() != null) {
				for (String email : user.getEmailAddresses()) {
					sb.append(",");
					sb.append(email);
				}
			}
			pfAccount.setEmail(sb.toString());
		}
		pfAccount.setNotes("Created from Webreg");
		pfAccount.setSpaceAllowed(spaceAllowed);
		pfAccount.setFirstname(user.getGivenName());
		pfAccount.setSurname(user.getSurName());
		pfAccount.setCustom3("webreg-active");
		pfAccount.setValidTil(dateFormat.format(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 50)));
		
		pfWorker.storeAccount(pfAccount);
		pfAccount = pfWorker.getAccountInfoByUsername(user.getEppn());
		pfWorker.setActivationDate(pfAccount);
		registry.getRegistryValues().put("powerfolderId", pfAccount.getId());		
	}

	@Override
	public void deregisterUser(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {

		PropertyReader prop = PropertyReader.newRegisterPropReader(service);
		PFApi14Worker pfWorker = new PFApi14Worker(prop, auditor);

		String userId = registry.getRegistryValues().get("powerfolderId");
		
		DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
		
		PFAccount pfAccount = pfWorker.getAccountInfoById(userId);
		
		if (pfAccount == null) {
			logger.warn("Account userId was deleted in Powerfolder system. Deregister is resumed.");
			return;
		}
		
		pfAccount.setId(userId);
		if (user.getEppn() != null)
			pfAccount.setUsername(user.getEppn());
		pfAccount.setNotes("Deactivated from Webreg");
		if (user.getGivenName() != null)
			pfAccount.setFirstname(user.getGivenName());
		if (user.getSurName() != null)
			pfAccount.setSurname(user.getSurName());
		pfAccount.setSpaceAllowed("0");
		pfAccount.setCustom3("webreg-inactive");
		pfAccount.setValidTil(dateFormat.format(new Date(System.currentTimeMillis())));
		
		pfWorker.storeAccount(pfAccount);
		
	}

	@Override
	public void reconciliation(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		
	}

	@Override
	public Boolean updateRegistry(UserEntity user, ServiceEntity service,
			RegistryEntity registry, Auditor auditor) throws RegisterException {
		return false;
	}

	@Override
	public Infotainment getInfo(RegistryEntity registry, UserEntity user, ServiceEntity service) throws RegisterException {
		PropertyReader prop = PropertyReader.newRegisterPropReader(service);

		PFApi14Worker pfWorker = new PFApi14Worker(prop, null);
		
		if (! registry.getRegistryValues().containsKey("powerfolderId"))
			throw new RegisterException("Registration is incomplete (missing powerfolderId)");
		
		String userId = registry.getRegistryValues().get("powerfolderId");

		PFAccount pfAccount = pfWorker.getAccountInfoById(userId);

		Infotainment info = new Infotainment();
		InfotainmentTreeNode root = new InfotainmentTreeNode("root", null);
		if (pfAccount == null) {
			logger.warn("Account {} has been deleted on PowerFolder Server", userId);
			new InfotainmentTreeNode("Fehler", "Account nicht gefunden", root);			
		}
		else {
			info.setMessage("Account geladen");
			
			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			
			InfotainmentTreeNode userRoot = new InfotainmentTreeNode("User Data", root);
			new InfotainmentTreeNode("Username", pfAccount.getUsername(), userRoot);
			new InfotainmentTreeNode("Firstname", pfAccount.getFirstname(), userRoot);
			new InfotainmentTreeNode("Surname", pfAccount.getSurname(), userRoot);
			new InfotainmentTreeNode("E-Mail", pfAccount.getEmail(), userRoot);
			new InfotainmentTreeNode("Allowed Space", decimalFormat.format(Long.parseLong(pfAccount.getSpaceAllowed()) / 1024 / 1024 / 1024) + " Gb", userRoot);
			new InfotainmentTreeNode("Used Space", decimalFormat.format(Long.parseLong(pfAccount.getSpaceUsed()) / 1024 / 1024 / 1024) + " Gb", userRoot);
			new InfotainmentTreeNode("Created", pfAccount.getCreated(), userRoot);
		}
		info.setRoot(root);
		return info;
			
	}
	
	@Override
	public Infotainment getInfoForAdmin(RegistryEntity registry, UserEntity user,
			ServiceEntity service) throws RegisterException {
		return getInfo(registry, user, service);
	}
}
