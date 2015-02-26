package edu.kit.scc.webreg.dto.mapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;

import edu.kit.scc.webreg.dto.entity.AbstractBaseEntityDto;
import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class AbstractBaseEntityMapper<T extends BaseEntity<PK>,E extends BaseEntityDto<PK>, PK extends Serializable>
		implements BaseEntityMapper<T, E, PK>, Serializable {

	private static final long serialVersionUID = 1L;

    protected abstract void copyAllProperties(T fromBaseEntity, E toDtoEntity); 

    public abstract Class<E> getEntityDtoClass();

	@Override
	public void copyProperties(T fromBaseEntity, E toDtoEntity) {
		toDtoEntity.setId(fromBaseEntity.getId());
		if ((fromBaseEntity instanceof AbstractBaseEntity) 
				&& (toDtoEntity instanceof AbstractBaseEntityDto)) {
//			AbstractBaseEntity f = (AbstractBaseEntity) fromBaseEntity;
//			AbstractBaseEntityDto t = (AbstractBaseEntityDto) toDtoEntity;
//			t.setCreatedAt(f.getCreatedAt());
//			t.setUpdatedAt(f.getUpdatedAt());
//			t.setVersion(f.getVersion());
			copy(fromBaseEntity, toDtoEntity, "createdAt", "updatedAt", "version");
		}
		
		copy(fromBaseEntity, toDtoEntity, getPropertiesToCopy());
		copyAllProperties(fromBaseEntity, toDtoEntity);
	}
	
	protected String[] getPropertiesToCopy() {
		return new String[]{};
	}
	
	protected void copy(T fromBaseEntity, E toDtoEntity, String... props) {
		for (String prop : props) {
			try {
				Object o = PropertyUtils.getProperty(fromBaseEntity, prop);
				PropertyUtils.setProperty(toDtoEntity, prop, o);
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (NoSuchMethodException e) {
			}
		}
	}
}
