package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzContentCurrentVersion implements Serializable {
		
	private static final long serialVersionUID = 7900747481418858631L;

	@JsonProperty("_media-type")
	private String mediaType;

	@JsonProperty("_href")
	private String href;
	
	@JsonProperty("Version")
	private EzVersion version;

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public EzVersion getVersion() {
		return version;
	}

	public void setVersion(EzVersion version) {
		this.version = version;
	}
	
}
