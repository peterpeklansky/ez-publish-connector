package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzLocationsResponse implements Serializable {
	
	private static final long serialVersionUID = -2340099015732921341L;
	
	@JsonProperty("Location")
	private EzLocation location;

	public EzLocation getLocation() {
		return location;
	}

	public void setLocation(EzLocation location) {
		this.location = location;
	}
	
	
}
