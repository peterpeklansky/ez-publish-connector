package org.mule.modules.ezpublish.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzRelation implements Serializable {
		
	private static final long serialVersionUID = 2775819544233297365L;

	@JsonProperty("SourceContent")
	private EzHref sourceContent;

	@JsonProperty("DestinationContent")
	private EzHref destinationContent;
	
	@JsonProperty("SourceFieldDefinitionIdentifier")
	private String sourceFieldDefinitionIdentifier;
	
	@JsonProperty("RelationType")
	private String relationType;

	public EzHref getSourceContent() {
		return sourceContent;
	}

	public void setSourceContent(EzHref sourceContent) {
		this.sourceContent = sourceContent;
	}

	public EzHref getDestinationContent() {
		return destinationContent;
	}

	public void setDestinationContent(EzHref destinationContent) {
		this.destinationContent = destinationContent;
	}

	public String getSourceFieldDefinitionIdentifier() {
		return sourceFieldDefinitionIdentifier;
	}

	public void setSourceFieldDefinitionIdentifier(
			String sourceFieldDefinitionIdentifier) {
		this.sourceFieldDefinitionIdentifier = sourceFieldDefinitionIdentifier;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	
	
	
}
