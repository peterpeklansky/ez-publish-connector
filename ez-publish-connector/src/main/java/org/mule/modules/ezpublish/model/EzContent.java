package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzContent implements Serializable {
	
	private static final long serialVersionUID = -1904100124179538359L;

	@JsonProperty("_media-type")
	private String mediaType;
	
	@JsonProperty("_remoteId")
	private String remoteId;
	
	@JsonProperty("_id")
	private int id;

	@JsonProperty("Name")
	private String name;
	
	@JsonProperty("CurrentVersion")
	private EzContentCurrentVersion currentVersion;
	
	@JsonProperty("ContentType")
	private EzContentType contentType;
	
	@JsonProperty("MainLocation")
	private EzContentMainLocation mainLocation;
	
	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EzContentCurrentVersion getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(EzContentCurrentVersion currentVersion) {
		this.currentVersion = currentVersion;
	}

	public EzContentMainLocation getMainLocation() {
		return mainLocation;
	}

	public void setMainLocation(EzContentMainLocation mainLocation) {
		this.mainLocation = mainLocation;
	}

	public EzContentType getContentType() {
		return contentType;
	}

	public void setContentType(EzContentType contentType) {
		this.contentType = contentType;
	}

	
}
