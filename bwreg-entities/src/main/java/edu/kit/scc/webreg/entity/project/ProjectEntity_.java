package edu.kit.scc.webreg.entity.project;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import edu.kit.scc.webreg.entity.AdminRoleEntity;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ProjectEntity.class)
public abstract class ProjectEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<ProjectEntity, AdminRoleEntity> adminRole;
	public static volatile SingularAttribute<ProjectEntity, String> description;
	public static volatile SingularAttribute<ProjectEntity, ProjectEntity> parentService;
	public static volatile SingularAttribute<ProjectEntity, String> shortDescription;
	public static volatile SingularAttribute<ProjectEntity, String> name;
	public static volatile SingularAttribute<ProjectEntity, String> shortName;

}

