package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

//@JsonIgnoreProperties(ignoreUnknown=true)
public class EzContentTypeResponse implements Serializable {
	
	private static final long serialVersionUID = 1123652425738776897L;
	
	@JsonProperty("ContentType")
	private EzContentType contentType;

	public EzContentType getContentType() {
		return contentType;
	}

	public void setContentType(EzContentType contentType) {
		this.contentType = contentType;
	}
	
	
}
