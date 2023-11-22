package edu.kit.scc.webreg.sec;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedirectMap {

	private Map<String, String[]> map;
	
	@PostConstruct
	public void init() {
		map = new HashMap<String, String[]>();
		map.put("pjt", new String[] { "^(.*)$", "/project/join-token.xhtml?token=$1" });
	}
	
	public String resolveRedirect(String path) {
		if (! path.startsWith("/r/")) {
			throw new IllegalArgumentException("Path must start with /r/");
		}
		else {
			path = path.substring(3);
			if (! path.contains("/")) {
				throw new IllegalArgumentException("Path must contain /");
			}
			else {
				String[] split = path.split("/");
				if (split.length != 2) {
					throw new IllegalArgumentException("Path must contain exactly one /");
				}
				else {
					String key = split[0];
					String value = split[1];
					String[] regex = map.get(key);
					String redirect = value.replaceAll(regex[0], regex[1]);
					return redirect;
				}
			}
		}
	}
}
