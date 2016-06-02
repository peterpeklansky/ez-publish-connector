package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EzContentInfo implements Serializable {

	private static final long serialVersionUID = 9156854850311312190L;

	@JsonProperty("Content")
	private EzContent content;

	public EzContent getContent() {
		return content;
	}

	public void setContent(EzContent content) {
		this.content = content;
	}
}
