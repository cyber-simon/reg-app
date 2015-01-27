package edu.kit.scc.webreg.service.reg;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.impl.GroupUpdateStructure;

public interface GroupCapable {

	void deleteGroup(GroupEntity group, ServiceEntity service, Auditor auditor)
			throws RegisterException;

	void updateGroups(ServiceEntity service, GroupUpdateStructure updateStruct,
			Auditor auditor) throws RegisterException;
}
