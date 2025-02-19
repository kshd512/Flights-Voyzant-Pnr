package com.mmt.flights.entity.cms;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

@XmlRootElement(name = "CRED_DETAIL_RESPONSE")
public class CMSDetailResponse {
	private HashMap<String,String> propMaps = new HashMap<String,String>();
	
	@JsonProperty("props")
	@XmlElement(name = "props")
	public HashMap<String, String> getPropMap() {
		return propMaps;
	}
	public void setPropMap(HashMap<String, String> propMap) {
		this.propMaps = propMap;
	}
}
