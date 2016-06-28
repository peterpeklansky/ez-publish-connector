package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzPriceZoneCountry implements Serializable {
		
	private static final long serialVersionUID = 4543562816173185259L;

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String name;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
		
}
