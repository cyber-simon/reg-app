package edu.kit.scc.webreg.service.attribute.proc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProcessor implements ValueProcessor {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private ValueUpdater valueUpdater;

	public void setValueUpdater(ValueUpdater valueUpdater) {
		this.valueUpdater = valueUpdater;
	}

	protected ValueUpdater getValueUpdater() {
		return valueUpdater;
	}
}
