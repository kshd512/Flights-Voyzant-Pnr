package com.mmt.flights.pnr.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.cms.CMSService;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.entity.cms.CMSDetailRequest;
import com.mmt.flights.entity.cms.CMSDetailResponse;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class CMSManagerTask implements MapTask {

    @Autowired
    private CMSService service;

    @Override
    public FlowState run(FlowState state) throws Exception {
        String cmsId = state.getValue(FlowStateKey.CMS_ID);
        CMSMapHolder cmsMapHolder = getCmsMap(cmsId);
        return state.toBuilder().addValue(FlowStateKey.CMS_MAP, cmsMapHolder).build();
    }

    public CMSMapHolder getCmsMap(String cmsId) {

        if (StringUtils.isBlank(cmsId)) {
            throw new PSErrorException("CMS ID is not available in state", PSCommonErrorEnum.CMS_DETAILS_RETRIEVE_FAILURE);
        }

        CMSDetailRequest cmsDetailRequest = new CMSDetailRequest();
        cmsDetailRequest.setCredID(cmsId);
        CMSDetailResponse cmsData = service.getCredentialDetail(cmsDetailRequest);
        validateCMSData(cmsData.getPropMap());

        return new CMSMapHolder(cmsId, cmsData.getPropMap());

    }

    private void validateCMSData(HashMap<String, String> propMap) {
        if (!propMap.containsKey(CMSConstants.SIGN)
                || !propMap.containsKey(CMSConstants.USERNAME)
                || !propMap.containsKey(CMSConstants.HOST)
        ) {
            throw new PSErrorException("required data not found in CMS MAP", PSCommonErrorEnum.INVALID_CREDENTIAL_ID);
        }
    }
}
