package org.mule.modules.ezpublish.model.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product implements Serializable {
		
	private static final long serialVersionUID = -1023208447116674655L;

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String code;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private List<String> features;
	
	@JsonProperty
	private List<String> legacyCodes;
	
	@JsonProperty
	private String type;
	
	@JsonProperty
	private String taxProfile;
	
	@JsonProperty
	private String startDate;
	
	@JsonProperty
	private String endDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getFeatures() {
		return features;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
	}

	public List<String> getLegacyCodes() {
		return legacyCodes;
	}

	public void setLegacyCodes(List<String> legacyCodes) {
		this.legacyCodes = legacyCodes;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTaxProfile() {
		return taxProfile;
	}

	public void setTaxProfile(String taxProfile) {
		this.taxProfile = taxProfile;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	
}
