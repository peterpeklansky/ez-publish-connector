package org.mule.modules.ezpublish.model.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PriceZone implements Serializable {

	private static final long serialVersionUID = 7094809863299251294L;

	@JsonProperty
	private String id;
		
	@JsonProperty
	private String name;
	
	@JsonProperty
	private String dispatch;
	
	@JsonProperty
	private String currency;
	
	@JsonProperty
	private String region;

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

	public String getDispatch() {
		return dispatch;
	}

	public void setDispatch(String dispatch) {
		this.dispatch = dispatch;
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
	
}
