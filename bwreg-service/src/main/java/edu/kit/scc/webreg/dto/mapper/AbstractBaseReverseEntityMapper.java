package edu.kit.scc.webreg.dto.mapper;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class AbstractBaseReverseEntityMapper<E extends BaseEntityDto<PK>, T extends BaseEntity<PK>, PK extends Serializable>
		implements BaseReverseEntityMapper<E, T, PK>, Serializable {

	private static final long serialVersionUID = 1L;

    protected abstract void copyAllProperties(E fromBaseEntity, T toDtoEntity); 

	protected String[] getPropertiesToCopy() {
		return new String[]{};
	}
	
	@Override
	public void copyProperties(E fromBaseEntity, T toDtoEntity) {
		
		copy(fromBaseEntity, toDtoEntity, getPropertiesToCopy());
		copyAllProperties(fromBaseEntity, toDtoEntity);
	}

	protected void copy(E fromBaseEntity, T toDtoEntity, String... props) {
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
