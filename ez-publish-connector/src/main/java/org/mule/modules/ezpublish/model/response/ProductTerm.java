package org.mule.modules.ezpublish.model.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductTerm implements Serializable {
		
	private static final long serialVersionUID = -3253143238063832285L;

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String code;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private String initialTermQuantity;
	
	@JsonProperty
	private String initialTermUnit;
	
	@JsonProperty
	private String effectiveFromDate;
	
	@JsonProperty
	private String effectiveToDate;
	
	@JsonProperty
	private Product product;

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

	public String getInitialTermQuantity() {
		return initialTermQuantity;
	}

	public void setInitialTermQuantity(String initialTermQuantity) {
		this.initialTermQuantity = initialTermQuantity;
	}

	public String getInitialTermUnit() {
		return initialTermUnit;
	}

	public void setInitialTermUnit(String initialTermUnit) {
		this.initialTermUnit = initialTermUnit;
	}

	public String getEffectiveFromDate() {
		return effectiveFromDate;
	}

	public void setEffectiveFromDate(String effectiveFromDate) {
		this.effectiveFromDate = effectiveFromDate;
	}

	public String getEffectiveToDate() {
		return effectiveToDate;
	}

	public void setEffectiveToDate(String effectiveToDate) {
		this.effectiveToDate = effectiveToDate;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	
}
