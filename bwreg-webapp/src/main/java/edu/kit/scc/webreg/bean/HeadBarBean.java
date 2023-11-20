package edu.kit.scc.webreg.bean;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ResourceBundleHelper;

@Named("headBarBean")
@RequestScoped
public class HeadBarBean {

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private HttpServletRequest request;

	@Inject
	private SessionManager session;

	@Inject
	private ResourceBundleHelper resourceHelper;

	public ApplicationConfig getAppConfig() {
		return appConfig;
	}

	public String getStylesheet() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_css", "");
	}

	public String getStylesheetExtended() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_css_ext", "/resources/css/std-ext.css");
	}

	public String getOverrideStdStylesheet() {
		return getApplicationBase()
				+ getOrDefault(request.getServerName() + "_override_std_css", "/resources/css/std.css");
	}

	public String getHeaderImage() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_header_icon", "/resources/img/logo.svg");
	}

	public String getHeaderLink() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_header_link", "https://");
	}

	public String getHeaderTitle() {
		return getApplicationBase()
				+ getOrDefault(request.getServerName() + "_header_title", "FeLS - Federated Login Service");
	}

	public String getAppTitle() {
		return getApplicationBase()
				+ getOrDefault(request.getServerName() + "_app_title", "FeLS - Federated Login Service");
	}

	public String getAppImage() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_app_image", "/resources/img/regapp.jpg");
	}

	public String getAppLink() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_app_link", "https://");
	}

	public String getImpressum() {
		return resolve(getApplicationBase() + getOrDefault(request.getServerName() + "_impressum",
				"<a href=\"https://www.scc.kit.edu/impressum.php\" target=\"_blank\">???imprint???</a>"));
	}

	public String getDataProtection() {
		return resolve(getApplicationBase() + getOrDefault(request.getServerName() + "_data_protection",
				"<a href=\"https://www.scc.kit.edu/datenschutz.php\" target=\"_blank\">???data_privacy???</a>"));
	}

	public String getHomeLink() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_home_link",
				"<a href=\"http://www.kit.edu\" target=\"_blank\"><span class=\"svg-icon\"><img src=\"/resources/img/kitlogo_nano.svg\" width=\"10px\"/></span><span>KIT</span></a>");
	}

	public String getApplicationBase() {
		return getOrDefault(request.getServerName() + "_app_base", "");
	}

	public String getFooterLeft() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_footer_left",
				"KIT - The Research University in the Helmholtz Association");
	}

	public String getFooterRight() {
		String std = "<a href=\"https://git.scc.kit.edu/reg-app\"><span>reg-app by KIT</span></a>";
		return getApplicationBase() + getOrDefault(request.getServerName() + "_footer_right", std);
	}

	public String getSocial() {
		return getOrDefault(request.getServerName() + "_social",
				"<li><a href=\"https://twitter.com/#!/SCC_KIT\" class=\"twitter\" title=\"Twitter Kanal\" target=\"_blank\"><span>Twitter Kanal</span></a></li>");
	}

	public String getLocale() {
		if (session.getLocale() == null) {
			return FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
		}
		return session.getLocale();
	}

	public void setLocale(String locale) {
		session.setLocale(locale);
	}

	public SelectItem[] getLocales() {
		SelectItem[] selectItems = new SelectItem[appConfig.getLocaleMap().size()];
		int i = 0;
		for (Entry<String, String> entry : appConfig.getLocaleMap().entrySet()) {
			selectItems[i] = new SelectItem(entry.getKey(), entry.getValue());
			i++;
		}
		return selectItems;
	}

	public String getBurgerIcon() {
		return "<button class=\"burger\"><svg class=\"burger-icon\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 300 274.5\" width=\"300px\" height=\"274.5px\">\n"
				+ "    <rect class=\"burger-top\" y=\"214.4\" width=\"300\" height=\"60.1\"/>\n"
				+ "    <rect class=\"burger-middle\" y=\"107.2\" width=\"300\" height=\"60.1\"/>\n"
				+ "    <rect class=\"burger-bottom\" y=\"0\" width=\"300\" height=\"60.1\"/>\n" + "</svg></button>";
	}

	public String getOrDefault(String key, String defaultString) {
		if (appConfig.getConfigValue(key) != null) {
			return appConfig.getConfigValue(key);
		} else {
			return defaultString;
		}
	}

	private String resolve(String input) {
		Pattern p = Pattern.compile("(\\?\\?\\?.*?\\?\\?\\?)");
		Matcher m = p.matcher(input);
		StringBuffer output = new StringBuffer();
		int prevEnd = 0;
		while (m.find()) {
			String key = m.group(1);
			String value = key.substring(3, key.length() - 3);
			output.append(input.substring(prevEnd, m.start()));
			output.append(resourceHelper.resolveMessage(value));
			prevEnd = m.end();
		}
		output.append(input.substring(prevEnd));
		return output.toString();
	}
}