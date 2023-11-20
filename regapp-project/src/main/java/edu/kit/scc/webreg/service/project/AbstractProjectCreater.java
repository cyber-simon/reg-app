package edu.kit.scc.webreg.service.project;

import java.io.Serializable;

import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.event.EventSubmitter;

public class AbstractProjectCreater<T extends ProjectEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private EventSubmitter eventSubmitter;
	
		
}
