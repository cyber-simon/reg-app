package edu.kit.scc.webreg.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2015-12-17T10:35:37.778+0100")
@StaticMetamodel(AbstractBaseEntity.class)
public class AbstractBaseEntity_ {
	public static volatile SingularAttribute<AbstractBaseEntity, Long> id;
	public static volatile SingularAttribute<AbstractBaseEntity, Date> createdAt;
	public static volatile SingularAttribute<AbstractBaseEntity, Date> updatedAt;
	public static volatile SingularAttribute<AbstractBaseEntity, Integer> version;
}
