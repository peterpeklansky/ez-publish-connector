package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EzContentType implements Serializable {

	private static final long serialVersionUID = -9088343184156611927L;

	@JsonProperty("_href")
	private String href;

	@JsonProperty("id")
	private int id;
	
	@JsonProperty("identifier")
	private String identifier;
	
	@JsonProperty("FieldDefinitions")
	private EzFieldDefinitions fieldDefinitions;

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

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

	public EzFieldDefinitions getFieldDefinitions() {
		return fieldDefinitions;
	}

	public void setFieldDefinitions(EzFieldDefinitions fieldDefinitions) {
		this.fieldDefinitions = fieldDefinitions;
	}
	
	
}
