package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzVersion implements Serializable {
		
	private static final long serialVersionUID = -5403467538553512208L;

	@JsonProperty("VersionInfo")
	private EzVersionInfo versionInfo;

	@JsonProperty("Fields")
	private EzFields fields;
	
	@JsonProperty("Relations")
	private EzRelations relations;

	public EzVersionInfo getVersionInfo() {
		return versionInfo;
	}

	public void setVersionInfo(EzVersionInfo versionInfo) {
		this.versionInfo = versionInfo;
	}

	public EzFields getFields() {
		return fields;
	}

	public void setFields(EzFields fields) {
		this.fields = fields;
	}

	public EzRelations getRelations() {
		return relations;
	}

	public void setRelations(EzRelations relations) {
		this.relations = relations;
	}
	
	
}
