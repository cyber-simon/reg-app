package edu.kit.scc.webreg.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogHelper {

	
	public StringBuffer convertStacktrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.getBuffer();
	}
}
