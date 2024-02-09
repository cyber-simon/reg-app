package edu.kit.scc.webreg.service.attribute.proc;

import java.util.function.Function;

import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public interface SingularAttributePipe<T extends ValueEntity, R extends ValueEntity> extends Function<T, R> {
	
}
