package org.mule.modules.ezpublish.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EzFieldDefinition implements Serializable {

	private static final long serialVersionUID = 8993871073354010194L;

	@JsonProperty("id")
	private int id;

	@JsonProperty("identifier")
	private String identifier;
	
//	@JsonProperty("fieldSettings")
//	private EzFieldSettings fieldSettings;
	@JsonProperty("fieldSettings")
	private Object fieldSettings;


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Object getFieldSettings() {
		return fieldSettings;
	}

	public void setFieldSettings(Object fieldSettings) {
		this.fieldSettings = fieldSettings;
	}

//	public EzFieldSettings getFieldSettings() {
//		return fieldSettings;
//	}
//
//	public void setFieldSettings(EzFieldSettings fieldSettings) {
//		this.fieldSettings = fieldSettings;
//	}
	
	
	
}
