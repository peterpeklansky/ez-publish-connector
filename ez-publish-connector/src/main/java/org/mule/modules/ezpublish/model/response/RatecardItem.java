package org.mule.modules.ezpublish.model.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatecardItem implements Serializable {
		
	private static final long serialVersionUID = 1128546848940482542L;

	@JsonProperty
	private String id;
	
	@JsonProperty
	private String rateCardHeaderCode;
	
	@JsonProperty
	private String rateCardHeaderName;
	
	@JsonProperty
	private String code;
	
	@JsonProperty
	private String name;
	
	@JsonProperty
	private BigDecimal initialRate;
	
	@JsonProperty
	private Boolean autoRenew;

	@JsonProperty
	private BigDecimal followUpRate;
	
	@JsonProperty
	private String followUpTermQuantity;
	
	@JsonProperty
	private String followUpTermUnit;
	
	@JsonProperty
	private ProductTerm productTerm;
	
	@JsonProperty
	private String priceZoneName;
	
	@JsonProperty
	private String priceZoneDispatch;
	
	@JsonProperty
	private String priceZoneCurrency;
	
	@JsonProperty
	private String priceZoneRegion;
	
	@JsonProperty
	private List<String> priceZoneCountries;

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

	public BigDecimal getInitialRate() {
		return initialRate;
	}

	public void setInitialRate(BigDecimal initialRate) {
		this.initialRate = initialRate;
	}

	public Boolean getAutoRenew() {
		return autoRenew;
	}

	public void setAutoRenew(Boolean autoRenew) {
		this.autoRenew = autoRenew;
	}

	public BigDecimal getFollowUpRate() {
		return followUpRate;
	}

	public void setFollowUpRate(BigDecimal followUpRate) {
		this.followUpRate = followUpRate;
	}

	public String getFollowUpTermQuantity() {
		return followUpTermQuantity;
	}

	public void setFollowUpTermQuantity(String followUpTermQuantity) {
		this.followUpTermQuantity = followUpTermQuantity;
	}

	public String getFollowUpTermUnit() {
		return followUpTermUnit;
	}

	public void setFollowUpTermUnit(String followUpTermUnit) {
		this.followUpTermUnit = followUpTermUnit;
	}

	public ProductTerm getProductTerm() {
		return productTerm;
	}

	public void setProductTerm(ProductTerm productTerm) {
		this.productTerm = productTerm;
	}

	public String getPriceZoneName() {
		return priceZoneName;
	}

	public void setPriceZoneName(String priceZoneName) {
		this.priceZoneName = priceZoneName;
	}

	public String getPriceZoneDispatch() {
		return priceZoneDispatch;
	}

	public void setPriceZoneDispatch(String priceZoneDispatch) {
		this.priceZoneDispatch = priceZoneDispatch;
	}

	public String getPriceZoneCurrency() {
		return priceZoneCurrency;
	}

	public void setPriceZoneCurrency(String priceZoneCurrency) {
		this.priceZoneCurrency = priceZoneCurrency;
	}

	public String getPriceZoneRegion() {
		return priceZoneRegion;
	}

	public void setPriceZoneRegion(String priceZoneRegion) {
		this.priceZoneRegion = priceZoneRegion;
	}

	public String getRateCardHeaderCode() {
		return rateCardHeaderCode;
	}

	public void setRateCardHeaderCode(String rateCardHeaderCode) {
		this.rateCardHeaderCode = rateCardHeaderCode;
	}

	public String getRateCardHeaderName() {
		return rateCardHeaderName;
	}

	public void setRateCardHeaderName(String rateCardHeaderName) {
		this.rateCardHeaderName = rateCardHeaderName;
	}

	public List<String> getPriceZoneCountries() {
		return priceZoneCountries;
	}

	public void setPriceZoneCountries(List<String> priceZoneCountries) {
		this.priceZoneCountries = priceZoneCountries;
	}
	
	
}
