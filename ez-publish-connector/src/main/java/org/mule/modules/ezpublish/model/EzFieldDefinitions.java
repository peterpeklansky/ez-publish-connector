package org.mule.modules.ezpublish.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EzFieldDefinitions implements Serializable {

	private static final long serialVersionUID = -7830464128862363365L;
	
	@JsonProperty("FieldDefinition")
	private List<EzFieldDefinition> fieldDefinition;

	public List<EzFieldDefinition> getFieldDefinition() {
		return fieldDefinition;
	}

	public void setFieldDefinition(List<EzFieldDefinition> fieldDefinition) {
		this.fieldDefinition = fieldDefinition;
	}
	
}
