package com.mmt.flights.entity.cms;

import java.util.Map;

public class CMSMapHolder {

	private String cmsId;
	
	private Map<String, String> cmsMap;

	public CMSMapHolder(String cmsId, Map<String, String> cmsMap) {
		this.cmsMap = cmsMap;
		this.cmsId = cmsId;
	}


	public Map<String, String> getCmsMap() {
		return cmsMap;
	}

	public void setCmsMap(Map<String, String> cmsMap) {
		this.cmsMap = cmsMap;
	}

	public String getCmsId() {
		return cmsId;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
	
	public String getValue(String key) {
		return cmsMap.get(key);
	}

}
