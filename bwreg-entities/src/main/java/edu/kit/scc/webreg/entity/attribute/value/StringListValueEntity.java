package edu.kit.scc.webreg.entity.attribute.value;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;

@Entity(name = "StringListValueEntity")
public class StringListValueEntity extends ValueEntity {

	private static final long serialVersionUID = 1L;

    @ElementCollection
    @CollectionTable(
        name="value_string_list",
        joinColumns = @JoinColumn(name="value_id")
    )
	@Column(name = "value_string", length = 4096)
	private List<String> valueList;

	public List<String> getValueList() {
		return valueList;
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

}
