package com.mmt.flights.cms;

import com.mmt.flights.cms.hystrix.CMSDetailsHystrixCommand;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.entity.cms.CMSDetailRequest;
import com.mmt.flights.entity.cms.CMSDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class CMSEngineServiceHelper {
	
	@Autowired
	private CMSDetailsHystrixCommand cmsDetailsHystrixCommand;

	public Map<String, String> getCredentialMap(String credID) {
		Map<String, String> cmsDetailsMap = null;
		if (null != credID && !"".equals(credID)) {
			try {
				cmsDetailsMap = getCMSPropMap(credID, null, null);
			} catch (Exception e) {
				StringBuilder sb = new StringBuilder(credID)
						.append(" Error in fetching CMS properties for CredID : ");
				sb.append(credID);
			}
		}

		return cmsDetailsMap;
	}

	public Map<String, Map<String, String>> getCredentialMap(Map<String, String> credIDMap, String logKey,
			String controllerServiceName) {
		Map<String, Map<String, String>> airPropMap = new HashMap<>();
		Set<Map.Entry<String, String>> credIdSet = credIDMap.entrySet();
		Iterator<Map.Entry<String, String>> credIdIt = credIdSet.iterator();

		while (credIdIt.hasNext()) {
			Map.Entry<String, String> credEntry = credIdIt.next();
			String airline = credEntry.getKey();
			String credID = credEntry.getValue();
			if (null != credID && !"".equals(credID)) {
				try {
					// we are taking default name from Amadeus config file, although name will be
					// same across all the airlines. i.e. CRED_ID
					// Map will be null, if CMS details has got some error.
					Map<String, String> cmsDetailsMap = getCMSPropMap(credID, controllerServiceName, logKey);
					if (cmsDetailsMap != null) {
						cmsDetailsMap.put("CRED_ID", credID);
						airPropMap.put(airline, cmsDetailsMap);
					}
				} catch (Exception e) {
					StringBuilder sb = new StringBuilder(logKey)
							.append(" Error in fetching CMS properties for CredID : ");
					sb.append(credID);
					MMTLogger.error(logKey,sb.toString(), CMSEngineServiceHelper.class.getName(),e);
				}
			}
		}
		// This will throw exception, if CMS details is not present for at least one
		// airline.
		if (airPropMap.isEmpty()) {
			String errorMsg = String.format("%s CMS Details is blank for all requested airlines.", logKey);
			MMTLogger.error(logKey,errorMsg, CMSEngineServiceHelper.class.getName(),null);
			throw new RuntimeException(errorMsg);
		}
		return airPropMap;
	}

	public Map<String, String> getCMSPropMap(String credID, String controllerServiceName, String logKey) {
		CMSDetailRequest cmdRequest = getCMSDetailRequest(credID);
		CMSDetailResponse response;
		try {
			response = cmsDetailsHystrixCommand.run(cmdRequest, logKey);
			return response.getPropMap();
		} catch (Exception e) {
			return null;
		}
	}

	private CMSDetailRequest getCMSDetailRequest(String credID) {
		CMSDetailRequest cmdRequest = new CMSDetailRequest();
		cmdRequest.setCredID(credID);
		return cmdRequest;
	}
}
