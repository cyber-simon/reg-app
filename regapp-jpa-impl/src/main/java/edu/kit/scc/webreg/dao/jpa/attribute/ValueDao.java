package edu.kit.scc.webreg.dao.jpa.attribute;

import java.util.ArrayList;
import java.util.List;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class ValueDao extends JpaBaseDao<ValueEntity> {

	@Override
	public Class<ValueEntity> getEntityClass() {
		return ValueEntity.class;
	}

	public void deleteAllDependingValues(ValueEntity value) {
		List<ValueEntity> deleteList = new ArrayList<>();
		deleteList.add(value);
		if (value.getNextValues().size() > 0) {
			while (value.getNextValues().iterator().hasNext()) {
				value = value.getNextValues().iterator().next();
				if (value.getPrevValues().size() == 1) {
					// only delete values with single previous value
					// if there are multiple values, the next value can't be removed
					deleteList.add(value);
				}
			}
		}

		deleteList.stream().forEach(v -> {
			delete(v);
		});
	}
}
