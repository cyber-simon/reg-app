package edu.kit.scc.webreg.bean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;

@Named("headBarBean")
@RequestScoped
public class HeadBarBean {

	@Inject
	private ApplicationConfig appConfig;

	@Inject 
	private HttpServletRequest request;
	
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
		return getApplicationBase() + getOrDefault(request.getServerName() + "_override_std_css", "/resources/css/std.css");
	}
	
	public String getHeaderImage() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_header_icon", "/resources/img/logo.svg");
	}
	
	public String getHeaderLink() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_header_link", "https://");
	}
	
	public String getHeaderTitle() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_header_title", "FeLS - Federated Login Service");
	}
	
	public String getAppTitle() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_app_title", "FeLS - Federated Login Service");
	}
	
	public String getAppImage() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_app_image", "/resources/img/regapp.jpg");
	}
	
	public String getAppLink() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_app_link", "https://");
	}
	
	public String getImpressum() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_impressum", "<a href=\"https://www.scc.kit.edu/impressum.php\" target=\"_blank\">Impressum</a>");
	}
	
	public String getDataProtection() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_data_protection", "<a href=\"https://www.scc.kit.edu/datenschutz.php\" target=\"_blank\">Datenschutz</a>");
	}
	
	public String getHomeLink() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_home_link", "<a href=\"http://www.kit.edu\" target=\"_blank\"><span class=\"svg-icon\"><img src=\"/resources/img/kitlogo_nano.svg\" width=\"10px\"/></span><span>KIT</span></a>");
	}
	
	public String getApplicationBase() {
		return getOrDefault(request.getServerName() + "_app_base", "");
	}

	public String getFooterLeft() {
		return getApplicationBase() + getOrDefault(request.getServerName() + "_footer_left", "KIT - The Research University in the Helmholtz Association");
	}

	public String getSocial() {
		return getOrDefault(request.getServerName() + "_social", "<li><a href=\"https://twitter.com/#!/SCC_KIT\" class=\"twitter\" title=\"Twitter Kanal\" target=\"_blank\"><span>Twitter Kanal</span></a></li>");
	}

	public String getBurgerIcon() {
		return "<button class=\"burger\"><svg class=\"burger-icon\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 300 274.5\" width=\"300px\" height=\"274.5px\">\n"
				+ "    <rect class=\"burger-top\" y=\"214.4\" width=\"300\" height=\"60.1\"/>\n"
				+ "    <rect class=\"burger-middle\" y=\"107.2\" width=\"300\" height=\"60.1\"/>\n"
				+ "    <rect class=\"burger-bottom\" y=\"0\" width=\"300\" height=\"60.1\"/>\n"
				+ "</svg></button>";
	}
	
	public String getOrDefault(String key, String defaultString) {
		if (appConfig.getConfigValue(key) != null) {
			return appConfig.getConfigValue(key);
		}
		else {
			return defaultString;
		}		
	}
}