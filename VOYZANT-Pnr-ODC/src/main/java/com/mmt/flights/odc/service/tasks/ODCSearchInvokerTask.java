package com.mmt.flights.odc.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.SupplierStep;
import com.mmt.flights.common.logging.TaskLog;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.common.util.HttpClientUtil;
import com.mmt.flights.config.ConnectorEndpoints;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODCSearchInvokerTask implements MapTask {

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.ORDER_RESHOP);
        String searchRequest = state.getValue(FlowStateKey.ODC_SEARCH_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = PSCommonErrorEnum.OK;
        String searchResponse = null;
        long startTime = System.currentTimeMillis();

        try {
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getOdcSearchURL();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(searchRequest));
            
            searchResponse = httpClientUtil.post(url, searchRequest, String.class, cmsMap.getCmsMap());
            taskLog.setResponse(MMTLogger.convertToJson(searchResponse));
        } catch (PSErrorException e) {
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e) {
            errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
            throw new PSErrorException("Error during ODC search network call: " + e.getMessage(), PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            taskLog.setError(errorCode);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.REQUEST_LATENCY.name(), startTime);
        }

        return state.toBuilder()
                .addValue(FlowStateKey.ODC_SEARCH_RESPONSE, searchResponse)
                .build();
    }
}