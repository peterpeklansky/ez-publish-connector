package org.mule.modules.ezpublish.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class EzRelations implements Serializable {
		
	private static final long serialVersionUID = 2781785298245225611L;
	
	@JsonProperty("Relation")
	private List<EzRelation> relation;

	public List<EzRelation> getRelation() {
		return relation;
	}

	public void setRelation(List<EzRelation> relation) {
		this.relation = relation;
	}
	
}
