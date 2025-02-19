package com.mmt.flights.entity.cms;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CRED_DETAIL_REQUEST")
public class CMSDetailRequest {
	private String credID;

	@JsonProperty("OfficeID")
	@XmlElement(name = "OfficeID")
	public String getCredID() {
		return credID;
	}
	public void setCredID(String credID) {
		this.credID = credID;
	}
}
