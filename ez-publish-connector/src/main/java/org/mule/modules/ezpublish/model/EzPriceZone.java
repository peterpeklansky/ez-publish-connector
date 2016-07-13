package org.mule.modules.ezpublish.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzPriceZone implements Serializable {
		
	private static final long serialVersionUID = 1551014147099887274L;

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private String currency;
	
	@JsonProperty
	private String region;
	
	@JsonProperty
	private String dispatch;
	
	@JsonProperty
	private List<EzPriceZoneCountry> countries;

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

	public List<EzPriceZoneCountry> getCountries() {
		return countries;
	}

	public void setCountries(List<EzPriceZoneCountry> countries) {
		this.countries = countries;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getDispatch() {
		return dispatch;
	}

	public void setDispatch(String dispatch) {
		this.dispatch = dispatch;
	}
		
}
