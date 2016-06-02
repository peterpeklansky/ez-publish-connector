package org.mule.modules.ezpublish.model.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ratecard implements Serializable {
		
	private static final long serialVersionUID = -7497394452440472975L;

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String code;
	
	@JsonProperty
	private String name;
	
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
