package edu.kit.scc.webreg.service.project;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.project.AttributeSourceProjectDao;
import edu.kit.scc.webreg.dao.project.BaseProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceStatusType;
import edu.kit.scc.webreg.entity.project.ProjectServiceType;

@ApplicationScoped
public class AttributeSourceProjectUpdater extends AbstractProjectUpdater<AttributeSourceProjectEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AttributeSourceProjectDao dao;

	@Inject
	private AttributeSourceProjectCreater projectCreater;

	@Inject
	private AttributeSourceProjectUpdater projectUpdater;

	public boolean syncAttributeSourceProjects(List<String> externalNamesList, UserEntity user,
			AttributeSourceEntity attributeSource) {
		boolean changed = false;

		IdentityEntity identity = user.getIdentity();

		List<ProjectIdentityAdminEntity> adminList = dao.findAdminByIdentity(identity);
		// TODO move this logic to dao query
		List<ProjectIdentityAdminEntity> filteredAdminList = adminList.stream()
				.filter(pia -> (pia.getProject() instanceof AttributeSourceProjectEntity)
						&& (((AttributeSourceProjectEntity) pia.getProject()).getAttributeSource()
								.equals(attributeSource)))
				.collect(Collectors.toList());

		logger.debug("Identity {} is admin in {} projects and in {} attribute sourced projects", identity.getId(),
				adminList.size(), filteredAdminList.size());

		for (ProjectIdentityAdminEntity pia : filteredAdminList) {
			if (!externalNamesList.contains(((AttributeSourceProjectEntity) pia.getProject()).getExternalName())) {
				logger.debug("Project {} no longer in external attribute source. Removing ProjectIdentityAdminEntity",
						pia.getProject().getId());
				dao.removeAdminFromProject(pia);
				changed = true;
			}
		}

		for (String name : externalNamesList) {
			AttributeSourceProjectEntity project = dao.findByExternalNameAttributeSource(name, attributeSource);

			if (filteredAdminList.stream()
					.filter(pia -> ((AttributeSourceProjectEntity) pia.getProject()).getExternalName().equals(name))
					.findFirst().isEmpty()) {
				logger.debug("Project {} not in ProjectIdentityAdminEntity for identity {}", name, identity.getId());

				if (project == null) {
					project = projectCreater.create(name, name, name, name, attributeSource);
				}

				dao.addAdminToProject(project, identity, ProjectAdminType.OWNER);
			}

			logger.debug("Checking if service connections are correct for {}", project.getName());
			Set<ServiceEntity> serviceList = attributeSource.getAttributeSourceServices().stream().map(asse -> asse.getService()).collect(Collectors.toSet());
			projectUpdater.updateServices(project, serviceList, ProjectServiceType.PASSIVE_GROUP,ProjectServiceStatusType.ACTIVE, "attribute-source-" + attributeSource.getId());
			
			// Add missing connections to service
//			for (AttributeSourceServiceEntity asse : attributeSource.getAttributeSourceServices()) {
//				if (project.getProjectServices().stream()
//						.noneMatch(ps -> ps.getService().equals(asse.getService()))) {
//					logger.debug("Connecting project {} with service {}", project.getName(), asse.getService().getName());
//					projectUpdater.addOrChangeService(project, asse.getService(), ProjectServiceType.PASSIVE_GROUP,
//							ProjectServiceStatusType.ACTIVE, "attribute-source-" + attributeSource.getId());
//				}
//			}
//
//			for (ProjectServiceEntity ps : project.getProjectServices()) {
//				if (attributeSource.getAttributeSourceServices().stream().noneMatch(asse -> asse.getService().equals(ps.getService()))) {
//					logger.debug("Removing connectiion: project {} with service {}", project.getName(), ps.getService().getName());
//					.
//				}
//			}
		}

		return changed;
	}

	@Override
	protected BaseProjectDao<AttributeSourceProjectEntity> getDao() {
		return dao;
	}
}
