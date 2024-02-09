package edu.kit.scc.webreg.service.attribute.proc;

import java.util.List;
import java.util.function.Function;

import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;

public interface MultiAttributePipe<T extends List<ValueEntity>, R extends ValueEntity> extends Function<T, R> {
	
}
