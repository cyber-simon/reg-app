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
package edu.kit.scc.webreg.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("themeSwitcherBean")
@ApplicationScoped
public class ThemeSwitcherBean implements Serializable {
        
	private static final long serialVersionUID = 1L;

	private Map<String, String> themes;
    
    private List<Theme> themeList;
    
    public Map<String, String> getThemes() {
        return themes;
    }

    @PostConstruct
    public void init() {
        themeList = new ArrayList<Theme>();
        themeList.add(new Theme("afterdark", "afterdark.png"));
        themeList.add(new Theme("afternoon", "afternoon.png"));
        themeList.add(new Theme("afterwork", "afterwork.png"));
        themeList.add(new Theme("aristo", "aristo.png"));
        themeList.add(new Theme("black-tie", "black-tie.png"));
        themeList.add(new Theme("blitzer", "blitzer.png"));
        themeList.add(new Theme("bluesky", "bluesky.png"));
        themeList.add(new Theme("bootstrap", "bootstrap.png"));
        themeList.add(new Theme("casablanca", "casablanca.png"));
        themeList.add(new Theme("cruze", "cruze.png"));
        themeList.add(new Theme("cupertino", "cupertino.png"));
        themeList.add(new Theme("dark-hive", "dark-hive.png"));
        themeList.add(new Theme("dot-luv", "dot-luv.png"));
        themeList.add(new Theme("eggplant", "eggplant.png"));
        themeList.add(new Theme("excite-bike", "excite-bike.png"));
        themeList.add(new Theme("flick", "flick.png"));
        themeList.add(new Theme("glass-x", "glass-x.png"));
        themeList.add(new Theme("home", "home.png"));
        themeList.add(new Theme("hot-sneaks", "hot-sneaks.png"));
        themeList.add(new Theme("humanity", "humanity.png"));
        themeList.add(new Theme("le-frog", "le-frog.png"));
        themeList.add(new Theme("midnight", "midnight.png"));
        themeList.add(new Theme("mint-choc", "mint-choc.png"));
        themeList.add(new Theme("overcast", "overcast.png"));
        themeList.add(new Theme("pepper-grinder", "pepper-grinder.png"));
        themeList.add(new Theme("redmond", "redmond.png"));
        themeList.add(new Theme("rocket", "rocket.png"));
        themeList.add(new Theme("sam", "sam.png"));
        themeList.add(new Theme("smoothness", "smoothness.png"));
        themeList.add(new Theme("south-street", "south-street.png"));
        themeList.add(new Theme("start", "start.png"));
        themeList.add(new Theme("sunny", "sunny.png"));
        themeList.add(new Theme("swanky-purse", "swanky-purse.png"));
        themeList.add(new Theme("trontastic", "trontastic.png"));
        themeList.add(new Theme("ui-darkness", "ui-darkness.png"));
        themeList.add(new Theme("ui-lightness", "ui-lightness.png"));
        themeList.add(new Theme("vader", "vader.png"));
        
        themes = new TreeMap<String, String>();
        themes.put("Afterdark", "afterdark");
        themes.put("Afternoon", "afternoon");
        themes.put("Afterwork", "afterwork");
        themes.put("Aristo", "aristo");
        themes.put("Black-Tie", "black-tie");
        themes.put("Blitzer", "blitzer");
        themes.put("Bluesky", "bluesky");
        themes.put("Bootstrap", "bootstrap");
        themes.put("Casablanca", "casablanca");
        themes.put("Cupertino", "cupertino");
        themes.put("Cruze", "cruze");
        themes.put("Dark-Hive", "dark-hive");
        themes.put("Dot-Luv", "dot-luv");
        themes.put("Eggplant", "eggplant");
        themes.put("Excite-Bike", "excite-bike");
        themes.put("Flick", "flick");
        themes.put("Glass-X", "glass-x");
        themes.put("Home", "home");
        themes.put("Hot-Sneaks", "hot-sneaks");
        themes.put("Humanity", "humanity");
        themes.put("Le-Frog", "le-frog");
        themes.put("Midnight", "midnight");
        themes.put("Mint-Choc", "mint-choc");
        themes.put("Overcast", "overcast");
        themes.put("Pepper-Grinder", "pepper-grinder");
        themes.put("Redmond", "redmond");
        themes.put("Rocket", "rocket");
        themes.put("Sam", "sam");
        themes.put("Smoothness", "smoothness");
        themes.put("South-Street", "south-street");
        themes.put("Start", "start");
        themes.put("Sunny", "sunny");
        themes.put("Swanky-Purse", "swanky-purse");
        themes.put("Trontastic", "trontastic");
        themes.put("UI-Darkness", "ui-darkness");
        themes.put("UI-Lightness", "ui-lightness");
        themes.put("Vader", "vader");
    }
    
    public List<Theme> getThemeList() {
        return themeList;
    }
}
