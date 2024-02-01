package edu.kit.scc.webreg.dao.jpa.attribute;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.attribute.ValueType;
import edu.kit.scc.webreg.entity.attribute.value.LongValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.NullValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.PairwiseIdentifierValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringListValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@Named
@ApplicationScoped
public class ValueDao extends JpaBaseDao<ValueEntity> {

	public ValueEntity createNew(ValueType valueType) {
		switch (valueType) {
		case STRING:
			return new StringValueEntity();
		case STRING_LIST:
			return new StringListValueEntity();
		case LONG:
			return new LongValueEntity();
		case PAIRWISE_ID:
			return new PairwiseIdentifierValueEntity();
		case NULL:
			return new NullValueEntity();
			
		default:
			throw new IllegalArgumentException("Unexpected value: " + valueType);
		}
	}

	@Override
	public Class<ValueEntity> getEntityClass() {
		return ValueEntity.class;
	}
}
