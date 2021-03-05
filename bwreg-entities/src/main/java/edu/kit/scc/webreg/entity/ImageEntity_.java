package edu.kit.scc.webreg.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ImageEntity.class)
public abstract class ImageEntity_ extends edu.kit.scc.webreg.entity.AbstractBaseEntity_ {

	public static volatile SingularAttribute<ImageEntity, ImageDataEntity> imageData;
	public static volatile SingularAttribute<ImageEntity, String> name;
	public static volatile SingularAttribute<ImageEntity, ImageType> imageType;

}

