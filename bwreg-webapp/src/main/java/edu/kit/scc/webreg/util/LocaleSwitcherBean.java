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
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named("localeSwitcherBean")
@ApplicationScoped
public class LocaleSwitcherBean implements Serializable {
        
	private static final long serialVersionUID = 1L;

    private List<Locale> localeList;
    
    @PostConstruct
    public void init() {
    	localeList = new ArrayList<Locale>();
    	localeList.add(Locale.GERMAN);
    	localeList.add(Locale.ENGLISH);
    }

	public List<Locale> getLocaleList() {
		return localeList;
	}
    
}
