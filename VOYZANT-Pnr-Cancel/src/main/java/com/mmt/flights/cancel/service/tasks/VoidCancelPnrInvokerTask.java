package com.mmt.flights.cancel.service.tasks;

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
import com.mmt.flights.util.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoidCancelPnrInvokerTask implements MapTask {

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private ConnectorEndpoints endpoints;

    @Autowired
    private LoggingService loggingService;

    @Override
    public FlowState run(FlowState state) throws Exception {
        TaskLog taskLog = new TaskLog(SupplierStep.VOID_ORDER);
        String voidPnrRequest = state.getValue(FlowStateKey.VOID_PNR_REQUEST);
        CMSMapHolder cmsMap = state.getValue(FlowStateKey.CMS_MAP);
        PSErrorEnum errorCode = PSCommonErrorEnum.OK;
        String voidPnrResponse = extractAndValidateVoidPnrResponse(state, taskLog, voidPnrRequest, cmsMap, errorCode);
        return state.toBuilder().addValue(FlowStateKey.VOID_PNR_RESPONSE, voidPnrResponse).build();
    }

    private String extractAndValidateVoidPnrResponse(FlowState state, TaskLog taskLog, String voidPnrRequest, 
                                                    CMSMapHolder cmsMap, PSErrorEnum errorCode) {
        String voidPnrResponse = null;
        long startTime = System.currentTimeMillis();
        try {
            String url = cmsMap.getCmsMap().get(CMSConstants.HOST) + endpoints.getPnrSplitURL();
            taskLog.setUrl(url);
            taskLog.setRequest(MMTLogger.convertToJson(voidPnrRequest));
            
            voidPnrResponse = httpClientUtil.post(url, voidPnrRequest, String.class, cmsMap.getCmsMap());
            taskLog.setResponse(MMTLogger.convertToJson(voidPnrResponse));
        } catch (PSErrorException e) {
            errorCode = e.getPsErrorEnum();
            throw e;
        } catch (Exception e) {
            errorCode = PSCommonErrorEnum.FLT_UNKNOWN_ERROR;
            throw new PSErrorException("Error during void PNR network call task: " + e, 
                                      PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        } finally {
            taskLog.setError(errorCode);
            loggingService.logEncrypted(state, taskLog);
            MMTLogger.logTimeForNetworkCall(state, MetricServices.PNR_VOID_NETWORK_CALL_LATENCY.name(), startTime);
        }
        return voidPnrResponse;
    }
}