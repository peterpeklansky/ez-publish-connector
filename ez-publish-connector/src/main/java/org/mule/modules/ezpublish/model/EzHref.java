package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzHref implements Serializable {
		
	private static final long serialVersionUID = 1277393967567641631L;
	
	@JsonProperty("_href")
	private String href;

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
}
