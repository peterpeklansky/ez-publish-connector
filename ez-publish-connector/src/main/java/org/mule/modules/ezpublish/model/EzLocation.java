package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EzLocation implements Serializable {

	private static final long serialVersionUID = -3835894494415182362L;

	@JsonProperty("ContentInfo")
	private EzContentInfo conetntInfo;

	public EzContentInfo getConetntInfo() {
		return conetntInfo;
	}

	public void setConetntInfo(EzContentInfo conetntInfo) {
		this.conetntInfo = conetntInfo;
	}

}
