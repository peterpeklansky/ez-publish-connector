package org.mule.modules.ezpublish.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzFields implements Serializable {
		
	private static final long serialVersionUID = 7301149915903480803L;
	
	@JsonProperty("field")
	private List<EzField> field;

	public List<EzField> getField() {
		return field;
	}

	public void setField(List<EzField> field) {
		this.field = field;
	}

	
	
}
