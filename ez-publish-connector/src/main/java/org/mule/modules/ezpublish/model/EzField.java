package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzField implements Serializable {
	
	private static final long serialVersionUID = 7328507923073072252L;

	@JsonProperty("fieldDefinitionIdentifier")
	private String fieldDefinitionIdentifier;

	@JsonProperty("fieldValue")
	private Object fieldValue;

	public String getFieldDefinitionIdentifier() {
		return fieldDefinitionIdentifier;
	}

	public void setFieldDefinitionIdentifier(String fieldDefinitionIdentifier) {
		this.fieldDefinitionIdentifier = fieldDefinitionIdentifier;
	}

	public Object getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(Object fieldValue) {
		this.fieldValue = fieldValue;
	}	
	
}
